class user_model{
  String name;
  String email;

  user_model(this.name, this.email);

  user_model.fromJson(Map<String,dynamic> json){
    name = json['username'];
    email = json['email'];
  }

}
