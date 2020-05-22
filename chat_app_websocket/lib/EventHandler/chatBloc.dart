import 'dart:async';
import 'dart:convert';
import 'chat_event.dart';
import 'package:chat_app_websocket/models/message_model.dart';

class chatBloc{
  var response;
  List<Message> messages;
  chat_event event;
  String current_chat_username,current_chat_email;
  String my_username,my_email;

  final _chatStateController = StreamController<List<Message>>();
  StreamSink<List<Message>> get _chatCounter => _chatStateController.sink;
  Stream<List<Message>> get chats => _chatStateController.stream;

  final _chatEventController = StreamController<chat_event>();
  Sink<chat_event> get chatEventsink => _chatEventController.sink;

  chatBloc(){
    event = getEvent(response);
    _chatEventController.stream.listen(_mapEventToState());
  }

  chat_event getEvent(var response){
    if(response==null){
      return NoActivityEvent();
    }else {
      var jsonString = String.fromCharCodes(response);
      var jsonRes = json.decode(jsonString);
      Message msg = new Message.fromJson(jsonRes);

      if (msg.from == current_chat_username)
        return currentChat();
      else if (msg.from == my_username)
        return currentChatMessageSent();
      else
        return externalChat();
    }
  }

   _mapEventToState(){
    if(event is externalChat){
        //Save the responses to database.
    }else if(event is currentChat){
       //Save the response to database.
      //Show the message to ui
      var res = String.fromCharCodes(response);
      var jsonString = json.decode(res);
      Message newMsg = new Message.fromJson(jsonString);
      messages.add(newMsg);
      _chatCounter.add(messages);
    }
    else if(event is NoActivityEvent){
      print("No activity is going on");
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



