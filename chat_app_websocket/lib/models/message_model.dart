class Message {
  String _id;
  String _from;
  String _to;
  String _message;
  String _media;
  int _time;

  Message.fromJson(Map<String, dynamic> json) {
    if (json['type'] == 'message' && json['event'] == 'personal') {
      Map<String, dynamic> details = json['details'];
      this._id = details['id'];
      this._from = details['from'];
      this._to = details['to'];
      this._message = details['message'];
      this._media = details['media'];
      this._time = details['time'];
    }
  }

  Map<String,dynamic> toJson ()=>{
    "type": "message",
    "event": "personal",
    "details": {
      "to": "$_to",
      "message": "$_message",
      "media": null,
      "time": new DateTime.now().millisecondsSinceEpoch
    }
  };

  Message.fromMap(Map<String, dynamic> message) {
    this._id = message['id'];
    this._from = message['_from'];
    this._to = message['_to'];
    this._message = message['message'];
    this._media = message['media'];
    this._time = message['time'];
  }

  bool hasMessage() {
    return this._message.isEmpty || this._message == null;
  }

  bool hasMedia() {
    return this._media.isEmpty || this._media == null;
  }

  String get id {
    return this._id;
  }

  String get from {
    return this._from;
  }

  String get to {
    return this._to;
  }

  String get media {
    return this._media;
  }

  String get message {
    return this._message;
  }

  int get time {
    return this._time;
  }

  set id(String id){
    this._id = id;
  }

  set from(String from){
    this._from = from;
  }

}
