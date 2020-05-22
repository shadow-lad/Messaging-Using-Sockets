class OtpVerificationModel {
  final String credential;

  OtpVerificationModel(this.credential);

  Map<String, dynamic> toJson() => {"type": "verify", "otp": credential};
}
