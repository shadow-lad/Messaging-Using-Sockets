class OtpVerificationModel {
  final String credential;

  OtpVerificationModel(this.credential);

  Map<String, dynamic> toJson() => {"request": "verify", "otp": credential};
}
