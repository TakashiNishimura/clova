package com.example.clova;

import com.example.clova.db.GardenItem;
import com.example.clova.db.GardenRepository;
import com.linecorp.clova.extension.boot.handler.annnotation.*;
import com.linecorp.clova.extension.boot.message.response.CEKResponse;
import com.linecorp.clova.extension.boot.message.speech.OutputSpeech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.dao.DataAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Optional;

@CEKRequestHandler
public class IntentHandler {

  private static final Logger log = LoggerFactory.getLogger(IntentHandler.class);
  private GardenRepository gardenRepository;

  @Autowired
  public IntentHandler(GardenRepository gardenRepository) {
    this.gardenRepository = gardenRepository;
  }

  // スキル起動時の処理
  @LaunchMapping
  CEKResponse handleLaunch() {
    // Clovaに話させる。スキル(Session)は終了しない
    return CEKResponse.builder()
      .outputSpeech(OutputSpeech.text("温度、湿度、光、水分の中から、なになにを教えて、と聞いてください。"
              + "みずやりをした場合は、みずやりしたよ。と報告してください。"))
      .shouldEndSession(false)
      .build();
  }

  // EnvIntent 発生時の処理。引数をスロット名と一致させること！
  @IntentMapping("EnvIntent")
  CEKResponse handleIntent(@SlotValue Optional<String> hatake) {
    // スロットタイプが聞き取れていれば callbackEnvメソッド を呼び出す
    // 聞き取れていなければ 聞き取れませんでした を結果にする
    String outputSpeechText = hatake
      .map(this::callbackEnv)
      .orElse("聞き取れませんでした");

    // Clovaに callbackEnv の結果を話させる。スキル(Session）は終了しない
    return CEKResponse.builder()
      .outputSpeech(OutputSpeech.text(outputSpeechText))
      .shouldEndSession(false)
      .build();
  }

  // 聞き取れたスロットタイプの内容か2ら、回答を作る
  private String callbackEnv(String hatake) {
    switch (hatake) {
      case "温度":
        return replyTemperature();
      case "湿度":
        return replyHumidity();
      case "光":
        return replyAnalog();
      case "水分":
        return replyMoisture();
      case "水やり":
        return replyWatering();
      case "水やったか":
        return replyWateringQuery();
      default:
        return "へーすごいですね";
    }
  }

  // 「キャンセル」と言われたときの処理
  @IntentMapping("Clova.CancelIntent")
  CEKResponse handleCancelIntent() {
    // Clovaに話させる。スキル(Session)も終了する
    return CEKResponse.builder()
      .outputSpeech(OutputSpeech.text("部屋の環境スキルを終了します。"))
      .shouldEndSession(true)
      .build();
  }

  // スキルの終了時の処理
  @SessionEndedMapping
  CEKResponse handleSessionEnded() {
    log.info("はたけくんBotを終了しました。");
    return CEKResponse.empty();
  }

  private String replyTemperature() {
    String url = "https://us.wio.seeed.io/v1/node/GroveTempHumD0/temperature?access_token=";
    String key = "a6d12fb410d75d342036d1b192f76afe";
    URI uri = URI.create(url + key);
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    try {

      Celsius_degree celsiusdegree = restTemplate.getForObject(uri, Celsius_degree.class);

      float celsius_degreeValue = celsiusdegree.getCelsius_degree();

      String reply = "";
      if (celsius_degreeValue <= 10) {
        reply = "さむすぎぃ、人間は気にならないのか、暖房をつけろ";
      }
      else if (celsius_degreeValue > 30) {
        reply = "あっつ～、無駄に疲れちまうよ、冷房をつけろ";
      }
      else {
        reply = "あ～いっすね～、快適快適";
      }
      return "温度は"
              + celsius_degreeValue
              + "度です。"
              + reply;

    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return "センサーに接続できていません";
    }
  }

  private String replyHumidity() {
    String url = "https://us.wio.seeed.io/v1/node/GroveTempHumD0/humidity?access_token=";
    String key = "a6d12fb410d75d342036d1b192f76afe";
    URI uri = URI.create(url + key);
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    try {

      Humidity humidity = restTemplate.getForObject(uri, Humidity.class);
      float humidityValue = humidity.getHumidity();
      String reply = "";

      if (humidityValue <= 30) {
        reply = "乾燥しすぎなのじゃ、加湿せんとおぬしも困るじゃろう";
      }
      else if (humidityValue > 80) {
        reply = "不快なのじゃあ～腐ってしまうのじゃあ～、除湿することを勧めるぞ";
      }
      else {
        reply = "よい感じなのじゃ、ほめてつかわすぞ";
      }
      return "湿度は"
              + humidityValue
              + "パーセントです。"
              + reply;

    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return "センサーに接続できていません";
    }

  }

  private String replyAnalog() {
    String url = "https://us.wio.seeed.io/v1/node/GenericAInA0/analog?access_token=";
    String key = "1224957e2c0aa40efa00a1205a1f5b4c";
    URI uri = URI.create(url + key);
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    try {

      Analog analog = restTemplate.getForObject(uri, Analog.class);
      float analogValue = analog.getAnalog();
      String reply = "";

      if (analogValue <= 100) {
        reply = "前がみえねぇ、光が、光が欲しい";
      }
      else if (analogValue > 800) {
        reply = "めがぁ～めがぁ～、まぶしすぎる、いったん日陰に行かないか";
      }
      else {
        reply = "チャージングゴー！、今日もいい天気";
      }
      return "光の量は"
              + analogValue
              + "です。"
              + reply;

    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return "センサーに接続できていません";
    }

  }

  private String replyMoisture() {
    String url = "https://us.wio.seeed.io/v1/node/GroveMoistureA0/moisture?access_token=";
    String key = "a6d12fb410d75d342036d1b192f76afe";
    URI uri = URI.create(url + key);
    RestTemplate restTemplate = new RestTemplateBuilder().build();
    try {

      Moisture moisture = restTemplate.getForObject(uri, Moisture.class);
      float moistureValue = moisture.getMoisture();
      String reply = "";

      if (moistureValue <= 10) {
        reply = "のど渇いた～ビールだせビール、じゃなくて、水をください";
      }
      else if (moistureValue > 1000) {
        reply = "溺れる！溺れる！、水が多いよ";
      }
      else {
        reply = "はぁ～生き返るわぁ～、水、適量よ";
      }
      return "土の水分量は"
              + moistureValue
              + "です。"
              + reply;

    } catch (HttpClientErrorException e) {
      e.printStackTrace();
      return "センサーに接続できていません";
    }
  }

  private String replyWatering() {
    String reply = "";
    try {
      var item = new GardenItem();
      gardenRepository.insert(item);
      reply = "水やりしたことを記録しました。";
    } catch (DataAccessException e) {
      e.printStackTrace();
      reply = "データベースの登録に失敗しました。";
    }
    return reply;
  }

  private String replyWateringQuery() {
    String reply = "";
    java.util.Date nowDate = new java.util.Date();
    var items = gardenRepository.select(nowDate);
    if (items.isEmpty()) {
      reply = "今日は水やりがまだのようです。";
    }
    else {
      reply = "今日の水やりは済んでいるようです。";
    }
    return reply;
  }

}