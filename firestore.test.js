const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTimestamp() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write favorites, collections, history, or preferences", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("favorites").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("collections").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("recentlyViewed").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("preferences").doc("settings").get());
});

test("Cross-user isolation: Bob cannot read or write Alice's data", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("favorites").doc("nat_01").set({
      userId: ALICE_UID,
      wallpaperId: "nat_01",
      createdAt: ts,
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("favorites").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("favorites").doc("nat_01").delete());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("collections").doc("col_1").set({
      userId: ALICE_UID,
      name: "Hijack",
      wallpaperIds: [],
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("Owner can create, update, and delete collections with schema enforcement", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();
  const colRef = aliceDb.collection("users").doc(ALICE_UID).collection("collections").doc("pastel_vibes");

  await assertSucceeds(
    colRef.set({
      userId: ALICE_UID,
      name: "Pastel Vibes",
      description: "Soft lavender and dawn tones",
      wallpaperIds: ["nat_01", "abs_02"],
      coverUrl: "https://images.unsplash.com/photo-1",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Shadow field attack must fail
  await assertFails(
    colRef.update({
      isAdmin: true,
      updatedAt: pastTimestamp(),
    })
  );

  // Empty name attack must fail
  await assertFails(
    colRef.update({
      name: "",
      updatedAt: pastTimestamp(),
    })
  );

  // Valid rename and item addition succeeds
  await assertSucceeds(
    colRef.update({
      name: "Morning Calm",
      wallpaperIds: ["nat_01", "abs_02", "dep_01"],
      updatedAt: pastTimestamp(),
    })
  );

  await assertSucceeds(colRef.delete());
});

test("Owner can write recentlyViewed and preferences with enum checks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("recentlyViewed").doc("amo_01").set({
      userId: ALICE_UID,
      wallpaperId: "amo_01",
      viewedAt: ts,
    })
  );

  const prefRef = aliceDb.collection("users").doc(ALICE_UID).collection("preferences").doc("settings");
  await assertSucceeds(
    prefRef.set({
      userId: ALICE_UID,
      themeMode: "DARK",
      amoledDark: true,
      imageQuality: "ORIGINAL",
      wifiOnlyDownloads: false,
      recentSearches: ["pastel", "amoled"],
      homeCategoryChipsEnabled: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    prefRef.update({
      homeCategoryChipsEnabled: true,
      updatedAt: pastTimestamp(),
    })
  );

  // Non-boolean homeCategoryChipsEnabled must fail
  await assertFails(
    prefRef.update({
      homeCategoryChipsEnabled: "yes",
      updatedAt: pastTimestamp(),
    })
  );

  // Invalid themeMode enum must fail
  await assertFails(
    prefRef.update({
      themeMode: "NEON_PINK",
      updatedAt: pastTimestamp(),
    })
  );
});
