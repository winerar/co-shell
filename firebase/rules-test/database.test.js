// Realtime Database rules for the sync bell (firebase/database.rules.json). Run: scripts/test-rules.sh.
import { test, before, after, beforeEach } from 'node:test';
import { readFileSync } from 'node:fs';
import { initializeTestEnvironment, assertFails, assertSucceeds } from '@firebase/rules-unit-testing';
import { ref, get, set, update, serverTimestamp } from 'firebase/database';

let env;
const owner = { uid: 'owner', email: 'owner@gmail.com' };
const wife = { uid: 'wife', email: 'wife@gmail.com' };
const stranger = { uid: 'stranger', email: 'x@gmail.com' };

const db = (u, verified = true) =>
  env.authenticatedContext(u.uid, { email: u.email, email_verified: verified }).database();
const ping = (u) => ({ at: serverTimestamp(), by: u.uid });

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-coshell',
    database: { rules: readFileSync(new URL('../database.rules.json', import.meta.url), 'utf8') },
  });
});
after(() => env.cleanup());
beforeEach(() => env.clearDatabase());

async function ownerRegistersFamily() {
  await assertSucceeds(set(ref(db(owner), 'families/main/members/owner'), true));
  await assertSucceeds(update(ref(db(owner), 'families/main/members'), { owner: true, wife: true }));
}

test('first member registers only themselves, and only while the list is empty', async () => {
  await assertFails(set(ref(db(owner), 'families/main/members/wife'), true));
  await assertFails(set(ref(db(owner, false), 'families/main/members/owner'), true));
  await ownerRegistersFamily();
  await assertFails(set(ref(db(stranger), 'families/main/members/stranger'), true));
  await assertFails(update(ref(db(stranger), 'families/main/members'), { stranger: true, owner: true }));
});

test('members value must be true', async () => {
  await assertFails(set(ref(db(owner), 'families/main/members/owner'), 'yes'));
});

test('members ring with server time and own uid; stranger cannot ring or listen', async () => {
  await ownerRegistersFamily();
  await assertSucceeds(set(ref(db(wife), 'families/main/ping'), ping(wife)));
  await assertSucceeds(get(ref(db(owner), 'families/main/ping')));
  await assertFails(set(ref(db(wife), 'families/main/ping'), ping(owner)));
  await assertFails(set(ref(db(wife), 'families/main/ping'), { at: 1, by: wife.uid }));
  await assertFails(set(ref(db(wife), 'families/main/ping'), { ...ping(wife), secret: 'x' }));
  await assertFails(set(ref(db(stranger), 'families/main/ping'), ping(stranger)));
  await assertFails(get(ref(db(stranger), 'families/main/ping')));
  await assertFails(get(ref(db(stranger), 'families/main/members')));
});
