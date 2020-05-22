import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:chat_app_websocket/database/database.dart';

import 'chat_event.dart';
import 'package:chat_app_websocket/models/message_model.dart';

class chatBloc{
  List<Message> messages = [];
  chat_event event;
  String current_chat_username,current_chat_email;
  String my_username,my_email;
  StreamSubscription socketListner;
  Socket tempSocket;
  Message lastMessageSent;


  final _chatStateController = StreamController<List<Message>>();
  StreamSink<List<Message>> get _chatCounter => _chatStateController.sink;
  Stream<List<Message>> get chats => _chatStateController.stream;

  final _chatEventController = StreamController<chat_event>();
  Sink<chat_event> get chatEventsink => _chatEventController.sink;

  chatBloc(){}

  void getMessages() async{
    List<Message> localMsgs = await DatabaseProvider.db.getMessagesByUser(current_chat_email);
    messages.insertAll(0, localMsgs);
    _chatCounter.add(messages);
  }

  startListening(){
    socketListner.onData((data) {
      Map<String,dynamic> resp = jsonDecode(String.fromCharCodes(data));
      if(resp['status']=='sent'){
        lastMessageSent.id = resp['id'];
        print("Bloc startListening() : ${lastMessageSent.message}");
        DatabaseProvider.db.insertMessage(lastMessageSent);
        messages.add(lastMessageSent);
        _chatCounter.add(messages);
      }
      else{
        event = getEvent(data);
        _mapEventToState();
      }
    });
//    event = getEvent(response);
    _chatEventController.stream.listen((msg){
      event = msg;
      _mapEventToState();
    });
  }

  chat_event getEvent(var response){
      var jsonString = String.fromCharCodes(response);
      var jsonRes = json.decode(jsonString);
      Message msg = new Message.fromJson(jsonRes);
      if (msg.from == current_chat_email)
        return currentChat(msg);
      else if (msg.from == my_email)
        return currentChatMessageSent(msg);
      else
        return externalChat(msg);
  }

   _mapEventToState(){
    if(event is externalChat){
        //Save the responses to database.
    }else if(event is currentChat){
       //Save the response to database.
      //Show the message to ui
//      var res = String.fromCharCodes();
//      var jsonString = json.decode(res);
//      Message newMsg = new Message.fromJson(jsonString);
      messages.add(event.response);
      DatabaseProvider.db.insertMessage(event.response);
      _chatCounter.add(messages);
    }
    else if(event is NoActivityEvent){
      print("No activity is going on");
    }
    else if(event is sendMessageEvent){
      tempSocket.writeln(jsonEncode(event.response.toJson()));
      lastMessageSent = event.response;
      print("_mapEventToState() : message : ${lastMessageSent.message}");
      lastMessageSent.from = my_email;
    }
  }

}

  //1 - Get the message

  //2 - Create stream and sink
  //3 - Create a function to decide the events.
  //4 - Call the decider function and save the value to and event variable
  //5 - Then execute function mapToEvent

//
//  void _mapEventToState(chat_event event){
//    if(event is externalChat){
//        //Save the responses to database.
//    }else if(event is currentChat){
//       //Save the response to database.
//      //Show the message to ui
//      var msg = json.decode(response);
//      _inChat.add(msg['message']);
//    }
//  }
//



