class SQLStatements {

  static const String CREATE_TABLE_MESSAGES = '''
    CREATE TABLE IF NOT EXISTS messages (
      id TEXT NOT NULL PRIMARY KEY,
      _from TEXT NOT NULL,
      _to TEXT NOT NULL,
      message TEXT,
      media TEXT,
      time INTEGER NOT NULL
    );
  ''';

  static const String INSERT_INTO_MESSAGES_WITHOUT_MEDIA = '''
    INSERT INTO messages (id, _from, _to, message, time) VALUES (?, ?, ?, ?, ?);
  ''';

  static const String INSERT_INTO_MESSAGES_WITH_MEDIA = '''
    INSERT INTO messages (id, _from, _to, message, media, time) VALUES (?, ?, ?, ?, ?, ?);
  ''';

  static const String INSERT_INTO_MESSAGES_WITHOUT_MESSAGE = '''
    INSERT INTO messages (id, _from, _to, media, time) VALUES (?, ?, ?, ?, ?);
  ''';

  static const String FETCH_MESSAGES = '''
    SELECT * FROM messages;
  ''';

  static const String FETCH_MESSAGES_BY_USER = '''
    SELECT * FROM messages WHERE _from=? OR _to=?;
  ''';

}