import 'package:flutter/material.dart';

class chat_room extends StatefulWidget{
  @override
  State<StatefulWidget> createState() {
    // TODO: implement createState
    return new chat_roomState();
  }

}

class chat_roomState extends State<chat_room>{
  @override
  Widget build(BuildContext context) {
    // TODO: implement build
    return Scaffold(
      appBar: AppBar(
//        backgroundColor:Colors.red,
        backgroundColor: Color(0xff269aff),
        centerTitle: true,
        title: ListTile(
//            contentPadding: EdgeInsets.all(5.0),
          title: Text("Sabih",
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            letterSpacing: 1.0,
            fontSize: 18.5,
          ),),
          leading: CircleAvatar(
            backgroundImage: AssetImage("images/sample.jpg"),
            radius: 18.0,
          ),
        )
      ),

      body: Stack(
        children: <Widget>[

          Container(
            height: MediaQuery.of(context).size.height,
            width: MediaQuery.of(context).size.width,
//            color: Color(0xffE48400).withOpacity(0.7)
//            color: Color(0xff0015cf).withOpacity(0.8),
          ),

          Positioned(
            bottom: 0.0,
            child: Container(
//              height: 70.0,
              width: MediaQuery.of(context).size.width,
              color: Colors.white,
              padding: EdgeInsets.only(bottom: 10.0,left: 15.0,right: 5.0),
              child: Row(
                children: <Widget>[
                  Flexible(
                    child: TextField(
                      decoration: InputDecoration(
                        border: InputBorder.none,
                        hintText: "Type here.."
                      ),
                      maxLines: null,
                    ),
                  ),

                  Container(
                    margin: EdgeInsets.all(5.0),
                    child: IconButton(
                      icon: Icon(Icons.send,
                        color: Color(0xff26fff8),),
                    ),
                  )

                ],
              ),
            ),
          )

        ],
      )

    );
  }
}