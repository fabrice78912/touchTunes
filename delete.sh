const emails = db.users.aggregate([
  { $group: { _id: "$email", count: { $sum: 1 }, ids: { $push: "$_id" } } },
  { $match: { count: { $gt: 1 } } }
]);

emails.forEach(doc => {
  // garde le premier ID
  const idsToRemove = doc.ids.slice(1);
  if (idsToRemove.length > 0) {
    db.users.deleteMany({ _id: { $in: idsToRemove } });
    print(`🧹 Supprimé ${idsToRemove.length} doublons pour l'email ${doc._id}`);
  }
});
