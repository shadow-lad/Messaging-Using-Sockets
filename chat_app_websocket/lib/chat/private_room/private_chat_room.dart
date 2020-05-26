import 'dart:async';
import 'dart:io';
import 'dart:typed_data';
import 'package:chat_app_websocket/EventHandler/chatBloc.dart';
import 'package:chat_app_websocket/EventHandler/chat_event.dart';
import 'package:chat_app_websocket/bloc/client.dart';
import 'package:chat_app_websocket/models/message_model.dart';
import 'package:flutter/material.dart';
import '../chat_room.dart';

class privateChatRoom extends StatefulWidget{
  final String my_email;
  final String my_name;
  final String reciever_mail,reciever_name;
//  final Socket c_rsocket;

//  final StreamSubscription<Uint8List> socketListener;

  const privateChatRoom({Key key, this.my_email, this.my_name,this.reciever_mail, this.reciever_name}) : super(key: key);
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
//    throw UnimplementedError();
    return new privateChatRoomState();
  }

}

class privateChatRoomState extends State<privateChatRoom>{

//  final _bloc = chatBloc();
//  List<Message> previousMessages = [];
//
////  void intializeMessages() async{
////    previousMessages = await DatabaseProvider.db.getMessagesByUser(widget.reciever_mail);
////    setState(() {
////      this.previousMessages = previousMessages;
////    });
////  }
//  void initializeBloc() async{
//    _bloc.current_chat_username = widget.reciever_name;
//    _bloc.current_chat_email = widget.reciever_mail;
//    _bloc.my_email = widget.my_email;
//    _bloc.my_username = widget.my_name;
//    _bloc.socketListner = widget.socketListener;
//    _bloc.tempSocket = widget.c_rsocket;
//    _bloc.startListening();
////    previousMessages = await DatabaseProvider.db.getMessagesByUser(widget.reciever_mail);
//    _bloc.getMessages();
//  }

  final Client myClient = Client.instance;
  List<Message> previousMessages = [];

  TextEditingController textToSend = new TextEditingController();

  void sendMessage(String message){
    Map<String,dynamic> sendMsg = {
      "type": "message",
      "event": "personal",
      "details": {
        "to": "${widget.reciever_mail}",
        "message": "${message}",
        "media": null,
        "time": new DateTime.now().millisecondsSinceEpoch
      }
    };
    myClient.sendPrivateMessage(sendMsg);
  }

  @override
  void initState() {
    super.initState();
    myClient.getPreviousMessages(widget.reciever_mail);
//    previousMessages = myClient.msgs;
  }

  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
        body: Container(
          height: MediaQuery.of(context).size.height,
          width: MediaQuery.of(context).size.width,
          child: Stack(
            children: <Widget>[

              StreamBuilder(
                stream: myClient.chats,
                initialData: [],
                builder: (context,snapshot){
                  return ListView.builder(
                      itemCount: snapshot.data.length,
                      itemBuilder: (context,index){
                        return ListTile(
                          title: Text("${snapshot.data[index].from}"),
                          subtitle: Text("${snapshot.data[index].message}"),
                        );
                      }
                  );
                },
              ),

              Positioned(
                left: 10.0,
                right: 10.0,
                bottom: 30.0,
                child: Container(
                  width: MediaQuery.of(context).size.width,
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: <Widget>[
                      Container(
//                      height:45,
                        width: MediaQuery.of(context).size.width*(3/4.1),
                        padding: EdgeInsets.only(left: 10.0,right: 10.0),
                        decoration: BoxDecoration(
                            border: Border.all(
                              color: Color(0xff4b1534),
                            ),
                            borderRadius: BorderRadius.all(
                                Radius.circular(33.5)
                            )
                        ),
                        child: TextField(
                          controller: textToSend,
                          decoration: InputDecoration(
                            border: InputBorder.none,
                            hintText: "Type here...",
                            hintStyle: TextStyle(
                              color: Color(0xff4b1534),
                            )
                          ),
                          style: TextStyle(
                            color: Color(0xff4b1534),
                            fontSize: 15.0,
                            fontWeight: FontWeight.w400,
                          ),
                          maxLines: null,
                          maxLength: null,
                        ),
                      ),

                      SizedBox(width: 7.0,),

                      //Search button
                      GestureDetector(
                        child: CircleAvatar(
                          radius: 20,
                          backgroundColor: Color(0xff4b1534),
                          child: Icon(Icons.send,color: Color(0xfff1d170),),
                        ),
                        onTap: (){
                          sendMessage(textToSend.text.toString().trim());
                        },
                      )
                    ],
                  ),
                ),
              )

            ],
          ),
//        child: StreamBuilder(
//          stream: _bloc.response,
//          initialData: "",
//          builder: (BuildContext context, AsyncSnapshot<String> snapshot){
//            return Column(
//              children: <Widget>[
//                Text(snapshot.data)
//              ],
//            );
//          },
//        )
        ),
    );
  }
}
