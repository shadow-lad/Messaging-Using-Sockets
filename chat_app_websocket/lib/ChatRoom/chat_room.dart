import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:typed_data';
import 'package:chat_app_websocket/models/user_model.dart';
import 'package:flutter/material.dart';

class ChatRoom extends StatefulWidget{

  final String email;
  final String name;
  final Socket c_rsocket;

  final StreamSubscription<Uint8List> socketListener;
  ChatRoom({this.email, this.name, this.socketListener, this.c_rsocket});

  @override
  State<StatefulWidget> createState() {
    return new ChatRoomState();
  }

}

class ChatRoomState extends State<ChatRoom>{

  Future<List<user_model>> requestUsers() async{
    Map<String,dynamic> getUsers = {
      'request':'users'
    };

    List<user_model> users = [];

    widget.c_rsocket.writeln(jsonEncode(getUsers));
    widget.socketListener.onData((data) {
      var resp = String.fromCharCodes(data);
      print(resp);
      var usr = json.decode(resp);
      print(usr['details']);
      var x = usr['details'];
      for(var us in x){
        users.add(user_model.fromJson(us));
      }
    });
    return users;
  }

  @override
  void initState() {
    super.initState(); 
    print("Logged in as : " + widget.email + " and username: " + widget.name);
    requestUsers();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: ListView(
        children: <Widget>[

          //For the heading
          Container(
//            height: MediaQuery.of(context).size.height*(1/3),
            padding: EdgeInsets.only(top:20.0,left: 20.0,right: 20.0,bottom: 10.0),
            child: Text("Chats",
              style: TextStyle(
                fontSize: 55,
                fontWeight: FontWeight.bold,
                color: Color(0xfff1d170)
              ),
            ),
          ),

          //Search bar and options
          Container(
//            height: MediaQuery.of(context).size.height*(1/3),
            padding: EdgeInsets.only(top:10.0,left: 20.0,right: 20.0,bottom: 10.0),
            child: Row(
              children: <Widget>[
                //Searchbar
                Container(
                  height:45,
                  width: MediaQuery.of(context).size.width*(3/4.1),
                  decoration: BoxDecoration(
                    border: Border.all(
                      color: Color(0xff4b1534),
                    ),
                    borderRadius: BorderRadius.all(
                      Radius.circular(33.5)
                    )
                  ),
                ),

                SizedBox(width: 7.0,),

                //Search button
                CircleAvatar(
                  radius: 20,
                  backgroundColor: Color(0xff4b1534),
                  child: Icon(Icons.search,color: Color(0xfff1d170),),
                )

              ],
            ),
          ),

          //chat list
          Container(
            height: MediaQuery.of(context).size.height,
            padding: EdgeInsets.only(top:10.0,bottom: 10.0),
            child: FutureBuilder(
              future: requestUsers(),
              builder: (BuildContext context, AsyncSnapshot snapshot){
                if(snapshot.data==null){
                  return Center(
                    child: CircularProgressIndicator(
                      valueColor: AlwaysStoppedAnimation(Color(0xff4b1534)),
                    ),
                  );
                }
                else {
//                  print(snapshot.data['details']);
//                  return Container();
                  return ListView.builder(
                    itemCount: snapshot.data.length,
                    itemBuilder: (BuildContext context,int index){
                      return ListTile(
                        leading: Icon(Icons.account_circle,color: Color(0xfff1d170),size: 57,),
                        title: Text("${snapshot.data[index].name}",
                          style: TextStyle(
                            color: Color(0xff4b1534),
                            fontWeight: FontWeight.w600,
                            fontSize: 25,
                          ),),

                        subtitle: Text("${snapshot.data[index].email}",
                          style: TextStyle(
                            color: Color(0xff4b1534),
                            fontWeight: FontWeight.w300,
                            fontSize: 12.5,
                          ),),

                      );
                    },
                  );
                }
              },
            )
          ),

        ],
      ),
    );
  }
}
