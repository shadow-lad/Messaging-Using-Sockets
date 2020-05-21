class SQLStatements {

  static const String CREATE_TABLE_MESSAGES = '''
    CREATE TABLE IF NOT EXISTS messages (
      id TEXT NOT NULL PRIMARY KEY,
      from TEXT NOT NULL,
      to TEXT NOT NULL,
      message TEXT,
      media TEXT,
      time INTEGER NOT NULL
    );
  ''';

  static const String INSERT_INTO_MESSAGES_WITHOUT_MEDIA = '''
    INSERT INTO messages (id, from, to, message, time) VALUES (?, ?, ?, ?, ?);
  ''';

  static const String INSERT_INTO_MESSAGES_WITH_MEDIA = '''
    INSERT INTO messages (id, from, to, message, media, time) VALUES (?, ?, ?, ?, ?, ?);
  ''';

  static const String INSERT_INTO_MESSAGES_WITHOUT_MESSAGE = '''
    INSERT INTO messages (id, from, to, media, time) VALUES (?, ?, ?, ?, ?);
  ''';

  static const String FETCH_MESSAGES = '''
    SELECT * FROM messages;
  ''';

}