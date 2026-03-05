/**
 * Quick script to seed a test community_alert document directly into Firestore.
 * Run from the /functions folder:
 *
 *   set GOOGLE_APPLICATION_CREDENTIALS=path\to\serviceAccountKey.json
 *   node seed_alerts.js
 *
 * OR if you're logged in via `firebase login`, use Application Default Credentials:
 *
 *   set GOOGLE_CLOUD_PROJECT=mindaguard-47461
 *   node seed_alerts.js
 */

const admin = require("firebase-admin");

admin.initializeApp({
  projectId: "mindaguard-47461",
});

const db = admin.firestore();

async function seed() {
  console.log("Writing test community alert...");

  const docRef = await db.collection("community_alerts").add({
    title: "TEST ALERT — FLOOD WARNING",
    location: "Quimpo Blvd, Davao City",
    description: "This is a test alert seeded from the script. If you can see this in the app, Firestore is working.",
    latitude: 7.0644,
    longitude: 125.6079,
    submittedBy: "SEED_SCRIPT",
    submittedByName: "Seed Script",
    hazardType: "FLOOD",
    timestamp: admin.firestore.Timestamp.now(),
    isVerified: false,
    verifiedBy: "",
    status: "pending",
  });

  console.log("SUCCESS — Document written with ID:", docRef.id);

  // Now read it back
  const snap = await db.collection("community_alerts").orderBy("timestamp", "desc").limit(5).get();
  console.log("\nAll community_alerts documents (" + snap.size + " total):");
  snap.forEach((doc) => {
    const d = doc.data();
    console.log(`  ${doc.id} => ${d.title} | ${d.location} | status=${d.status}`);
  });

  process.exit(0);
}

seed().catch((err) => {
  console.error("FAILED:", err.message);
  process.exit(1);
});

