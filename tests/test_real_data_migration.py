"""Verify the actual migration SQL against the exported Room schema."""
import json
import pathlib
import re
import sqlite3
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
DATA = ROOT / 'android/app/src/main/java/com/notiguard/data'

class RealDataMigrationTest(unittest.TestCase):
    def test_preserves_real_notifications_and_rules(self):
        db = sqlite3.connect(':memory:')
        schema = json.loads((ROOT / 'android/app/schemas/com.notiguard.data.NotiGuardDatabase/1.json').read_text())
        for entity in schema['database']['entities']:
            db.execute(entity['createSql'].replace('${TABLE_NAME}', entity['tableName']))
        for index, source in enumerate(['seed', 'android_listener', 'companion_sync'], 1):
            db.execute('INSERT INTO records (id,uid,packageName,appLabel,title,text,postedAt,blocked,ongoing,source) VALUES (?,?,?,?,?,?,?,?,?,?)',
                       (index, str(index), 'app', 'App', 'Title', 'Text', 1, 0, 0, source))
        db.execute("INSERT INTO app_rules VALUES ('app', 'App', 0, 1)")
        migration = re.search(r'db.execSQL\("([^"]+)"\)', (DATA / 'NotiGuardDatabase.kt').read_text(encoding='utf-8-sig')).group(1)
        db.execute(migration)
        self.assertEqual(db.execute('SELECT source FROM records ORDER BY id').fetchall(), [('android_listener',), ('companion_sync',)])
        self.assertEqual(db.execute('SELECT blocking FROM app_rules').fetchall(), [(0,)])
        db.execute(migration)
        self.assertEqual(db.execute('SELECT COUNT(*) FROM records').fetchone(), (2,))
        db.execute('DELETE FROM records')
        dao = (DATA / 'NotiGuardDao.kt').read_text(encoding='utf-8-sig')
        stats = next(query for query in re.findall(r'"""(.*?)"""', dao, re.S) if 'AS appCount' in query)
        self.assertEqual(db.execute(stats).fetchone(), (0, 0, 0))

if __name__ == '__main__':
    unittest.main()
