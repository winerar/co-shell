// Firestore security rules tests. Run: scripts/test-rules.sh (starts the emulator).
import { test, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, updateDoc, deleteDoc, arrayUnion, serverTimestamp, collection, query, where, getDocs, writeBatch, arrayRemove, deleteField } from 'firebase/firestore';

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
  // Rows are tombstones for members; only the owner wipes them (deleting the family, #653).
  await join(wife);
  await assertFails(deleteDoc(doc(db(wife), 'families/main/records/transaction_t1')));
  await assertSucceeds(deleteDoc(ref));
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
  await assertSucceeds(setDoc(doc(anon, 'feedback/f3'), { ...message, attachments: ['p1', 'p2'] }));
  await assertFails(setDoc(doc(anon, 'feedback/f4'), { ...message, attachments: 'p1' }));
});

test('feedback files: anyone adds a well-formed part, nobody reads or changes it', async () => {
  const anon = env.unauthenticatedContext().firestore();
  const part = { name: 'chek.pdf', mime: 'application/pdf', file: 0, part: 0, parts: 1, data: 'JVBERi0=', createdAt: new Date() };
  await assertSucceeds(setDoc(doc(anon, 'feedback_files/p1'), part));
  await assertFails(setDoc(doc(anon, 'feedback_files/p2'), { ...part, part: 1 }));
  await assertFails(setDoc(doc(anon, 'feedback_files/p3'), { ...part, data: 'x'.repeat(700001) }));
  await assertFails(setDoc(doc(anon, 'feedback_files/p4'), { ...part, extra: 1 }));
  await assertFails(getDoc(doc(anon, 'feedback_files/p1')));
  await assertFails(updateDoc(doc(anon, 'feedback_files/p1'), { data: 'AA==' }));
  await assertFails(deleteDoc(doc(anon, 'feedback_files/p1')));
});

// ---- many families, leaving, removal, deletion, limit (#653, #661) ----------------------------------------------

const BIG = 'fam-0123456789abcdefghij'; // new families get long random ids
const pointer = (fid) => ({ familyId: fid, updatedAt: serverTimestamp() });

async function createBig(u, fid = BIG) {
  const d = db(u);
  const batch = writeBatch(d);
  batch.set(doc(d, `families/${fid}`), family(u));
  batch.set(doc(d, `users/${u.uid}`), pointer(fid));
  return batch.commit();
}

async function joinBig(u, fid = BIG) {
  const d = db(u);
  const batch = writeBatch(d);
  batch.update(doc(d, `families/${fid}`), {
    memberUids: arrayUnion(u.uid), [`members.${u.uid}`]: { name: 'W', email: u.email.toLowerCase() },
  });
  batch.set(doc(d, `users/${u.uid}`), pointer(fid));
  // the invitation is used up: otherwise a removed person could use it to come back
  batch.delete(doc(d, `invites/${u.email.toLowerCase()}`));
  return batch.commit();
}

async function seedBigWithMember(u = wife) {
  await assertSucceeds(createBig(owner));
  await assertSucceeds(setDoc(doc(db(owner), `invites/${u.email.toLowerCase()}`),
    { familyId: BIG, invitedBy: owner.uid, createdAt: serverTimestamp() }));
  await assertSucceeds(joinBig(u));
}

test('anyone creates their own family with a long id and a pointer; short ids and strangers\' pointers are refused', async () => {
  await assertSucceeds(createBig(owner));
  await assertSucceeds(getDoc(doc(db(owner), `users/${owner.uid}`)));
  await assertFails(getDoc(doc(db(stranger), `users/${owner.uid}`)));
  await assertSucceeds(createBig(stranger, 'fam-zzzzzzzzzzzzzzzzzzzz'));
  await assertFails(getDoc(doc(db(stranger), `families/${BIG}`)));
  await assertFails(setDoc(doc(db(wife), 'families/short'), family(wife)));
  // a pointer to a family the person is not in
  await assertFails(setDoc(doc(db(stranger), `users/${stranger.uid}`), pointer(BIG)));
  await assertFails(setDoc(doc(db(stranger), `users/${owner.uid}`), pointer(BIG)));
});

test('a family holds at most 10 people', async () => {
  await assertSucceeds(createBig(owner));
  const nine = Array.from({ length: 8 }, (_, i) => `u${i}`); // with the owner — 9
  await env.withSecurityRulesDisabled(async (ctx) => {
    await updateDoc(doc(ctx.firestore(), `families/${BIG}`), {
      memberUids: [owner.uid, ...nine],
      members: { [owner.uid]: { name: 'O', email: owner.email }, ...Object.fromEntries(nine.map((u) => [u, { name: u, email: `${u}@x.com` }])) },
    });
  });
  await assertSucceeds(setDoc(doc(db(owner), 'invites/wife@gmail.com'),
    { familyId: BIG, invitedBy: owner.uid, createdAt: serverTimestamp() }));
  await assertSucceeds(joinBig(wife)); // the 10th
  const late = { uid: 'late', email: 'late@gmail.com' };
  await assertSucceeds(setDoc(doc(db(owner), 'invites/late@gmail.com'),
    { familyId: BIG, invitedBy: owner.uid, createdAt: serverTimestamp() }));
  await assertFails(joinBig(late)); // the 11th
});

test('a member leaves: only themselves, and the pointer goes with them; the owner cannot leave', async () => {
  await seedBigWithMember();
  const d = db(wife);
  const b = writeBatch(d);
  b.update(doc(d, `families/${BIG}`), { memberUids: arrayRemove(wife.uid), [`members.${wife.uid}`]: deleteField() });
  b.delete(doc(d, `users/${wife.uid}`));
  await assertSucceeds(b.commit());
  await assertFails(getDoc(doc(db(wife), `families/${BIG}`)));
  await assertFails(updateDoc(doc(db(owner), `families/${BIG}`), { memberUids: arrayRemove(owner.uid), [`members.${owner.uid}`]: deleteField() }));
});

test('the owner removes a member; a member cannot remove another or the owner', async () => {
  await seedBigWithMember();
  const third = { uid: 'third', email: 'third@gmail.com' };
  await assertSucceeds(setDoc(doc(db(owner), 'invites/third@gmail.com'), { familyId: BIG, invitedBy: owner.uid, createdAt: serverTimestamp() }));
  await assertSucceeds(joinBig(third));
  await assertFails(updateDoc(doc(db(wife), `families/${BIG}`), { memberUids: arrayRemove(third.uid), [`members.${third.uid}`]: deleteField() }));
  await assertFails(updateDoc(doc(db(wife), `families/${BIG}`), { memberUids: arrayRemove(owner.uid), [`members.${owner.uid}`]: deleteField() }));
  await assertFails(updateDoc(doc(db(owner), `families/${BIG}`), { memberUids: arrayRemove(wife.uid, third.uid), [`members.${wife.uid}`]: deleteField(), [`members.${third.uid}`]: deleteField() }));
  await assertSucceeds(updateDoc(doc(db(owner), `families/${BIG}`), { memberUids: arrayRemove(wife.uid), [`members.${wife.uid}`]: deleteField() }));
  // the removed person keeps their pointer (that is how their phone learns it) and can drop it
  await assertSucceeds(getDoc(doc(db(wife), `users/${wife.uid}`)));
  await assertFails(getDoc(doc(db(wife), `families/${BIG}`)));
  await assertSucceeds(deleteDoc(doc(db(wife), `users/${wife.uid}`)));
});

test('only the owner deletes the family: records, invitations, then the family itself', async () => {
  await seedBigWithMember();
  await assertSucceeds(setDoc(doc(db(wife), `families/${BIG}/records/transaction_t1`), record(wife)));
  await assertFails(deleteDoc(doc(db(wife), `families/${BIG}/records/transaction_t1`)));
  await assertFails(deleteDoc(doc(db(wife), `families/${BIG}`)));
  await assertSucceeds(deleteDoc(doc(db(owner), `families/${BIG}/records/transaction_t1`)));
  await assertSucceeds(setDoc(doc(db(owner), 'invites/x@gmail.com'), { familyId: BIG, invitedBy: owner.uid, createdAt: serverTimestamp() }));
  await assertSucceeds(deleteDoc(doc(db(owner), 'invites/x@gmail.com')));
  await assertSucceeds(deleteDoc(doc(db(owner), `families/${BIG}`)));
  await assertFails(getDoc(doc(db(stranger), `families/${BIG}`)));
  // the person's own pointer is theirs to drop
  await assertSucceeds(deleteDoc(doc(db(wife), `users/${wife.uid}`)));
});

// ---- invitation codes for the closed test (#666) ------------------------------------------------------------------

const CODE = 'K7QXM2PD9WTA';
const anon = () => env.unauthenticatedContext().firestore();

async function seedCode(over = {}) {
  await env.withSecurityRulesDisabled(async (ctx) => {
    await setDoc(doc(ctx.firestore(), `access_codes/${CODE}`),
      { label: 'Аня', createdAt: new Date(), revoked: false, maxDevices: 2, devices: [], ...over });
  });
}

const addDevice = (id, c = anon()) => updateDoc(doc(c, `access_codes/${CODE}`), { devices: arrayUnion(id) });

test('access code: anyone reads it by its exact id, nobody lists, creates or deletes codes', async () => {
  await seedCode();
  await assertSucceeds(getDoc(doc(anon(), `access_codes/${CODE}`)));
  await assertFails(getDocs(collection(anon(), 'access_codes')));
  await assertFails(setDoc(doc(anon(), 'access_codes/NEWCODE'), { label: 'x', revoked: false, maxDevices: 99, devices: [] }));
  await assertFails(setDoc(doc(db(owner), 'access_codes/NEWCODE'), { label: 'x', revoked: false, maxDevices: 99, devices: [] }));
  await assertFails(deleteDoc(doc(anon(), `access_codes/${CODE}`)));
});

test('access code: a device registers itself up to the limit, once each', async () => {
  await seedCode();
  await assertSucceeds(addDevice('phone-1'));
  await assertSucceeds(addDevice('phone-2', db(wife)));
  await assertFails(addDevice('phone-3')); // the third phone
  await assertSucceeds(getDoc(doc(anon(), `access_codes/${CODE}`)));
});

test('access code: nothing but one new device may change; a revoked code takes no devices', async () => {
  await seedCode({ devices: ['phone-1'] });
  const ref = doc(anon(), `access_codes/${CODE}`);
  await assertFails(updateDoc(ref, { revoked: true }));
  await assertFails(updateDoc(ref, { maxDevices: 99 }));
  await assertFails(updateDoc(ref, { label: 'другой' }));
  await assertFails(updateDoc(ref, { devices: [] })); // removing devices
  await assertFails(updateDoc(ref, { devices: ['phone-2', 'phone-3'] })); // swapping, two at once
  await assertFails(addDevice('phone-1')); // already there: nothing to add
  await seedCode({ revoked: true });
  await assertFails(addDevice('phone-9'));
});
