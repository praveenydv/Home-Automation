
#include <ESP8266WiFi.h>
#include <WiFiUdp.h>
#include <FirebaseArduino.h>
#include <Thread.h>
#include <LiquidCrystal.h>
#include <DHT.h>



#define FIREBASE_HOST "homecontrol-27e93.firebaseio.com"
#define FIREBASE_AUTH "1a2DvCZfQEq4SpUnAcn4Upb5BF0Qtc34jq8deoWt"
#define WIFI_SSID "1234"
#define WIFI_PASSWORD "87654321"
#define DHTPIN D0
#define DHTTYPE DHT11


const int RS = D10, EN = D8, d4 = D4, d5 = D3, d6 = D2, d7 = D1;
int l[2] = {0, 0};
int s[2]={0,0}
const int trig = D7;
const int echo = D9;
const int a[2] = {D5,D6};
const int grove = A0;
String room = "room01";
String date="01/01/01";
char HH = 5;
char MM = 30;
char hours, minutes, seconds;

unsigned int localPort = 2390; 
IPAddress timeServerIP; 
const char* ntpServerName = "time.nist.gov";

const int NTP_PACKET_SIZE = 48; 
byte packetBuffer[ NTP_PACKET_SIZE]; 
WiFiUDP udp;


LiquidCrystal lcd(RS, EN, d4, d5, d6, d7);
DHT dht(DHTPIN,DHTTYPE);
Thread datasentThread = Thread();
Thread datagetThread = Thread();

void to_get() {

  DynamicJsonBuffer jsonBuffer;

  String string = Firebase.getString(room);
  Serial.println(string);
  JsonObject& getData = jsonBuffer.parseObject(Firebase.getString(room));

   s[0]=l[0];
   s[1]=l[1];
  for (int i = 0; i < 2; i++) {
    l[i] = getData["L" + String(i + 1)];
    Serial.print("light get:-");
    Serial.println(l[i]);
  }
  for (int i = 0; i < 7; i++) {
    digitalWrite(a[i], l[i]);
  }

}


void to_send() {

  StaticJsonBuffer<1024> jsonBuffer;

  JsonObject& send_Data = jsonBuffer.createObject();

  int temp = dht.readTemperature();
  Serial.println(temp);
  digitalWrite(trig, LOW);
  delayMicroseconds(2);
  digitalWrite(trig, HIGH);
  delayMicroseconds(10);
  digitalWrite(trig, LOW);
  long duration = pulseIn(echo, HIGH);
  int distance = duration * 0.034 / 2;
  int door;
  if (distance > 4) {
    door = 1;
  }
  else {
    door = 0;
  }

  Serial.print("temp");
  Serial.println(temp);
  Serial.print("door");
  Serial.println(distance);

  Serial.print("light");
  Serial.println(l[0]);
  Serial.print("light");
  Serial.println(l[1]);
date =getTime();
lcd.cursor(0,0);
  lcd.print(date);

if(s[0]!=l[0]){
   lcd.cursor(0,1);
  lcd.print("Btn Press:1);
  
}
else if(s[1]!=l[1]){
   lcd.cursor(0,1);
  lcd.print("Btn Press:2);
  
}
 


  send_Data["temp"] = temp;
  send_Data["door"] = door;

  for (int i = 0; i < 2; i++) {
    send_Data["L" + String(i + 1)] = l[i];
  }

  
  String value = "";
  send_Data.printTo(value);

  Firebase.setString(room, value);
  Serial.println(value);
  delay(500);
}



unsigned long sendNTPpacket(IPAddress& address)
{
  Serial.println("sending NTP packet...");
  memset(packetBuffer, 0, NTP_PACKET_SIZE);
 packetBuffer[0] = 0b11100011;   // LI, Version, Mode
  packetBuffer[1] = 0;     // Stratum, or type of clock
  packetBuffer[2] = 6;     // Polling Interval
  packetBuffer[3] = 0xEC;  // Peer Clock Precision
  packetBuffer[12]  = 49;
  packetBuffer[13]  = 0x4E;
  packetBuffer[14]  = 49;
  packetBuffer[15]  = 52;
 
  udp.beginPacket(address, 123); //NTP requests are to port 123
  udp.write(packetBuffer, NTP_PACKET_SIZE);
  udp.endPacket();
}


void setup() {
  Serial.begin(115200);
  lcd.begin(16, 2);


  pinMode(grove, OUTPUT); // for  button press
  pinMode(temper, INPUT); // to measure temperature
  pinMode(echo, INPUT);  // to receive ultrasonic wave
  pinMode(trig, OUTPUT);  // to send ultrasonic wave


  for (int i = 0; i < 2; i++) {
    pinMode(a[i], OUTPUT);
    digitalWrite(a[i],LOW);
  }

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Wifi connecting...");
  while (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }
  digitalWrite(grove, HIGH); // to indicate wifi connection
 lcd.cursor(13,1);
  lcd.print("WS:1");
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());

  Serial.println("Firebase Connecting...");

  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  while (Firebase.failed()) {
    Serial.print("-");
    delay(10);
  }

  Serial.println("Firebase Connected");
 udp.begin(localPort);
  Serial.print("Local port: ");
  Serial.println(udp.localPort());


  datagetThread.onRun(to_get);
  datagetThread.setInterval(3);
  datasentThread.onRun(to_send);
  datasentThread.setInterval(3);
}

void reconnectFirebase() {
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Serial.print("Firebase connecting...");

  while (Firebase.failed()) {
    delay(100);
  }
}

void reconnectWifi() {
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  digitalWrite(grove, LOW);
   lcd.cursor(13,1);
  lcd.print("WS:0");

  Serial.print("Wifi connecting...");
  if (WiFi.status() != WL_CONNECTED) {
    delay(100);
  }

}

void loop() {

  if (WiFi.status() != WL_CONNECTED) {
    reconnectWifi();
  }
  digitalWrite(grove, HIGH);
  lcd.cursor(13,1);
  lcd.print("WS:1");

  if (Firebase.failed()) {
    reconnectFirebase();

  }

 if (datagetThread.shouldRun()) {
    datagetThread.run();

  }
  if (datasentThread.shouldRun()) {
    datasentThread.run();

  }


}


String getTime(){

  WiFi.hostByName(ntpServerName, timeServerIP); 

  sendNTPpacket(timeServerIP);
  delay(1000);
  
  int cb = udp.parsePacket();
  if (!cb) {
    Serial.println("no packet yet");
  }
  else {
    Serial.print("packet received, length=");
    Serial.println(cb);
    udp.read(packetBuffer, NTP_PACKET_SIZE); 

    unsigned long highWord = word(packetBuffer[40], packetBuffer[41]);
    unsigned long lowWord = word(packetBuffer[42], packetBuffer[43]);
     unsigned long secsSince1900 = highWord << 16 | lowWord;
    Serial.print("Seconds since Jan 1 1900 = " );
    Serial.println(secsSince1900);
  Serial.print("Unix time = ");
    const unsigned long seventyYears = 2208988800UL;
    unsigned long epoch = secsSince1900 - seventyYears;
    Serial.println(epoch);


    minutes = ((epoch % 3600) / 60);
    minutes = minutes + MM; 
    
    hours = (epoch  % 86400L) / 3600;    
    if(minutes > 59)
    {      
      hours = hours + HH + 1; 
      minutes = minutes - 60;
    }
    else
    {
      hours = hours + HH;
    }
    
    Serial.print("The UTC time is ");          
    Serial.print(hours,DEC); 
    

    if ( minutes < 10 ) {
      Serial.print('0');
    }    
    Serial.print(minutes,DEC); 
    Serial.print(':');
    
    seconds = (epoch % 60);
    if ( seconds < 10 ) {
      Serial.print('0');
    }
    Serial.println(seconds,DEC); 
  }
  
  String dt = String(hours)+":"+String(minutes)+":"+String(seconds);

  return dt;
  }
