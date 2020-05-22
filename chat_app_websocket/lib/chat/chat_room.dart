import 'package:chat_app_websocket/bloc/client.dart';
import 'package:chat_app_websocket/models/user_details.dart';
import 'package:flutter/material.dart';

class ChatRoom extends StatefulWidget {
  
  final Client client = Client.instance;

  ChatRoom();

  @override
  State<StatefulWidget> createState() {
    return new ChatRoomState();
  }
}

class ChatRoomState extends State<ChatRoom> {
  static List<UserDetails> users;

  @override
  void initState() {
    super.initState();
    print('Logged in as : ${widget.client.details.email} and username: ${widget.client.details.username}');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView(
        children: <Widget>[
          //For the heading
          Container(
//            height: MediaQuery.of(context).size.height*(1/3),
            padding: EdgeInsets.only(
                top: 20.0, left: 20.0, right: 20.0, bottom: 10.0),
            child: Text(
              "Chats",
              style: TextStyle(
                  fontSize: 55,
                  fontWeight: FontWeight.bold,
                  color: Color(0xfff1d170)),
            ),
          ),

          //Search bar and options
          Container(
//            height: MediaQuery.of(context).size.height*(1/3),
            padding: EdgeInsets.only(
                top: 10.0, left: 20.0, right: 20.0, bottom: 10.0),
            child: Row(
              children: <Widget>[
                //Searchbar
                Container(
                  height: 45,
                  width: MediaQuery.of(context).size.width * (3 / 4.1),
                  decoration: BoxDecoration(
                      border: Border.all(
                        color: Color(0xff4b1534),
                      ),
                      borderRadius: BorderRadius.all(Radius.circular(33.5))),
                ),

                SizedBox(
                  width: 7.0,
                ),

                //Search button
                CircleAvatar(
                  radius: 20,
                  backgroundColor: Color(0xff4b1534),
                  child: Icon(
                    Icons.search,
                    color: Color(0xfff1d170),
                  ),
                )
              ],
            ),
          ),

          //chat list
          Container(
              height: MediaQuery.of(context).size.height,
              padding: EdgeInsets.only(top: 10.0, bottom: 10.0),
              child: StreamBuilder(
                stream: widget.client.userList,
                initialData: null,
                builder: (BuildContext context, AsyncSnapshot snapshot) {
                  print('CHAT ROOM: StreamBuilder: ${snapshot.data}'); 
                  if (snapshot.data == null) {
                    print('CHAT ROOM: StreamBuilder built');
                    return Center(
                      child: CircularProgressIndicator(
                        valueColor: AlwaysStoppedAnimation(Color(0xff4b1534)),
                      ),
                    );
                  } else if (snapshot.data.isEmpty) {
                    print('CHAT ROOM: Server sent an empty list');
                    return Center(
                      child: Text(
                        "No users",
                        style: Theme.of(context).textTheme.subtitle1,
                      ),
                    );
                  } else {
                    print('CHAT ROOM: Server sent a proper user list');
                    return ListView.builder(
                      itemCount: snapshot.data.length,
                      itemBuilder: (BuildContext context, int index) {
                        return GestureDetector(
                          child: ListTile(
                            leading: Icon(
                              Icons.account_circle,
                              color: Color(0xfff1d170),
                              size: 57,
                            ),
                            title: Text(
                              "${snapshot.data[index].username}",
                              style: TextStyle(
                                color: Color(0xff4b1534),
                                fontWeight: FontWeight.w600,
                                fontSize: 25,
                              ),
                            ),
                            subtitle: Text(
                              "${snapshot.data[index].email}",
                              style: TextStyle(
                                color: Color(0xff4b1534),
                                fontWeight: FontWeight.w300,
                                fontSize: 12.5,
                              ),
                            ),
                          ),
                          onTap:
                              null /*(){
                          Navigator.of(context).push(
                            new MaterialPageRoute(builder: (BuildContext context)=>new privateChatRoom(
                              my_email: widget.email,
                              my_name: widget.name,
                              reciever_mail: snapshot.data[index].email,
                              reciever_name: snapshot.data[index].name,
                              c_rsocket: widget.c_rsocket,
                              socketListener: widget.socketListener,
                            ))
                          );
                        }*/
                          ,
                        );
                      },
                    );
                  }
                },
              )),
        ],
      ),
    );
  }
}
