import 'package:chat_app_websocket/models/message_model.dart';

abstract class chat_event{
  final Message response;
  chat_event(this.response);
}

class externalChat extends chat_event{
  externalChat(Message response) : super(response);
}

class currentChat extends chat_event{
  currentChat(Message response) : super(response);
}

class currentChatMessageSent extends chat_event{
  currentChatMessageSent(Message response) : super(response);
}

class NoActivityEvent extends chat_event{
  NoActivityEvent(Message response) : super(response);
}

class sendMessageEvent extends chat_event{
  sendMessageEvent(Message response) : super(response);
}