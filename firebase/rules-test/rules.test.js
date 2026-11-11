// Firestore security rules tests. Run: scripts/test-rules.sh (starts the emulator).
import { test, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc, arrayUnion, serverTimestamp, collection, query, where, getDocs } from 'firebase/firestore';

let env;
const owner = { uid: 'owner', email: 'owner@gmail.com' };
const wife = { uid: 'wife', email: 'Wife@Gmail.com' };
const stranger = { uid: 'stranger', email: 'x@gmail.com' };

const db = (u) => env.authenticatedContext(u.uid, { email: u.email, email_verified: true }).firestore();

const family = (u) => ({
  ownerUid: u.uid, memberUids: [u.uid],
  members: { [u.uid]: { name: 'Owner', email: u.email } }, createdAt: serverTimestamp(),
});

const record = (u, over = {}) => ({
  kind: 'transaction', id: 't1', updatedAt: 1, updatedBy: u.uid, deleted: false, json: '{}',
  serverUpdatedAt: serverTimestamp(), ...over,
});

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-coshell',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});
after(() => env.cleanup());
beforeEach(() => env.clearFirestore());

async function seedFamilyWithInvite() {
  await assertSucceeds(setDoc(doc(db(owner), 'families/main'), family(owner)));
  await assertSucceeds(setDoc(doc(db(owner), 'invites/wife@gmail.com'),
    { familyId: 'main', invitedBy: owner.uid, createdAt: serverTimestamp() }));
}

async function join(u) {
  return updateDoc(doc(db(u), 'families/main'), {
    memberUids: arrayUnion(u.uid), [`members.${u.uid}`]: { name: 'Wife', email: u.email.toLowerCase() },
  });
}

test('first user creates the only family; nobody can create a second one or take it over', async () => {
  await assertSucceeds(setDoc(doc(db(owner), 'families/main'), family(owner)));
  await assertFails(setDoc(doc(db(stranger), 'families/main'), family(stranger)));
  await assertFails(setDoc(doc(db(stranger), 'families/other'), family(stranger)));
});

test('family must be created with the creator as the only member', async () => {
  await assertFails(setDoc(doc(db(owner), 'families/main'), { ...family(owner), memberUids: [owner.uid, stranger.uid] }));
  await assertFails(setDoc(doc(db(owner), 'families/main'), { ...family(owner), ownerUid: stranger.uid }));
});

test('stranger cannot read the family, its records or join without an invite', async () => {
  await seedFamilyWithInvite();
  await assertFails(getDoc(doc(db(stranger), 'families/main')));
  await assertFails(getDoc(doc(db(stranger), 'families/main/records/transaction_t1')));
  await assertFails(join(stranger));
  await assertFails(setDoc(doc(db(stranger), 'families/main/records/transaction_t1'), record(stranger)));
});

test('invited user reads own invite, joins and then syncs', async () => {
  await seedFamilyWithInvite();
  await assertSucceeds(getDoc(doc(db(wife), 'invites/wife@gmail.com')));
  await assertSucceeds(getDoc(doc(db(wife), 'families/main')));
  await assertFails(setDoc(doc(db(wife), 'families/main/records/transaction_t1'), record(wife)));
  await assertSucceeds(join(wife));
  await assertSucceeds(setDoc(doc(db(wife), 'families/main/records/transaction_t1'), record(wife)));
  await assertSucceeds(getDoc(doc(db(owner), 'families/main/records/transaction_t1')));
});

test('invite needs a verified email', async () => {
  await seedFamilyWithInvite();
  const unverified = env.authenticatedContext(wife.uid, { email: wife.email, email_verified: false }).firestore();
  await assertFails(getDoc(doc(unverified, 'families/main')));
});

test('joining user cannot add someone else or change the owner', async () => {
  await seedFamilyWithInvite();
  await assertFails(updateDoc(doc(db(wife), 'families/main'), {
    memberUids: arrayUnion(wife.uid, stranger.uid), [`members.${wife.uid}`]: { name: 'W', email: 'wife@gmail.com' },
  }));
  await assertFails(updateDoc(doc(db(wife), 'families/main'), { ownerUid: wife.uid }));
});

test('stranger cannot read or create invites; members can list theirs', async () => {
  await seedFamilyWithInvite();
  await assertFails(getDoc(doc(db(stranger), 'invites/wife@gmail.com')));
  await assertFails(setDoc(doc(db(stranger), 'invites/x@gmail.com'),
    { familyId: 'main', invitedBy: stranger.uid, createdAt: serverTimestamp() }));
  await assertSucceeds(getDocs(query(collection(db(owner), 'invites'), where('familyId', '==', 'main'))));
  await assertFails(getDocs(query(collection(db(stranger), 'invites'), where('familyId', '==', 'main'))));
  await assertSucceeds(deleteDoc(doc(db(owner), 'invites/wife@gmail.com')));
});

test('records: wrong author, key, kind, client time or deletion are rejected', async () => {
  await seedFamilyWithInvite();
  const ref = doc(db(owner), 'families/main/records/transaction_t1');
  await assertFails(setDoc(ref, record(owner, { updatedBy: 'wife' })));
  await assertFails(setDoc(doc(db(owner), 'families/main/records/transaction_t2'), record(owner)));
  await assertFails(setDoc(doc(db(owner), 'families/main/records/secret_t1'), record(owner, { kind: 'secret' })));
  await assertFails(setDoc(ref, record(owner, { serverUpdatedAt: new Date(0) })));
  await assertFails(setDoc(ref, record(owner, { extra: 1 })));
  await assertSucceeds(setDoc(ref, record(owner, { updatedBy: null })));
  await assertFails(deleteDoc(ref));
});

test('feedback: anyone creates a well-formed message and reads it by id, but cannot list or change it', async () => {
  const anon = env.unauthenticatedContext().firestore();
  const message = {
    text: 'Ошибка: x', createdAt: new Date(), appVersion: '0.1.0', versionCode: 248, device: 'Xiaomi',
    os: 'Android 16', installationId: 'id', processed: false,
  };
  await assertSucceeds(setDoc(doc(anon, 'feedback/f1'), message));
  await assertFails(setDoc(doc(anon, 'feedback/f2'), { ...message, status: 'done' }));
  await assertSucceeds(getDoc(doc(anon, 'feedback/f1')));
  await assertFails(getDocs(collection(anon, 'feedback')));
  await assertFails(updateDoc(doc(anon, 'feedback/f1'), { text: 'другое' }));
  await assertFails(deleteDoc(doc(anon, 'feedback/f1')));
});
