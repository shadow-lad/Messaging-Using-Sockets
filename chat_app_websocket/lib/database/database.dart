import 'package:chat_app_websocket/database/sql_statements.dart';
import 'package:chat_app_websocket/models/message_model.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class DatabaseProvider {
  DatabaseProvider._();

  static final DatabaseProvider db = DatabaseProvider._();
  static Database _database;

  Future<Database> get database async {
    if (_database == null) {
      _database = await initDatabase();
    }
    return _database;
  }

  Future<Database> initDatabase() async {
    return await openDatabase(
      join(await getDatabasesPath(), 'database.db'),
      onCreate: (db, version) async {
        await db.execute(SQLStatements.CREATE_TABLE_MESSAGES);
      },
      version: 1
    );
  }

  insertMessage(Message message) async {
    final db = await database;

    return await db.rawInsert(
        SQLStatements.INSERT_INTO_MESSAGES,
        [message.id, message.from, message.to, message.message, message.media, message.time]
    );
  }

  Future<List<Message>> getMessages() async {
    final db = await database;
    var message = [];

    var results = await db.rawQuery(SQLStatements.FETCH_MESSAGES);

    for (var result in results) {
      if (result.isNotEmpty) {
         message.add(Message.fromMap(result));
      }
    }
    
    return message;
  }

  Future<List<Message>> getMessagesByUser(String email) async {
    final db = await database;
    List<Message> message = [];

    var results = await db.rawQuery(SQLStatements.FETCH_MESSAGES_BY_USER,
      [email, email]
    );

    for (var result in results) {
      if (result.isNotEmpty) {
        print(result);
        message.add(Message.fromMap(result));
      }
    }

    return message;
  }

}