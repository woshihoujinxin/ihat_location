package top.microfrank.ihat_location.deal;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import top.microfrank.ihat_location.model.Config;
import top.microfrank.ihat_location.model.L;

import javax.annotation.Resource;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
@Component
public class SendData {
    private static Timer timer;
    @Resource(name="senddata")
    public CopyOnWriteArrayList<L>[] data;
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private Config config;

    public void send() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                for (CopyOnWriteArrayList<L> hiddata : data) {
                    if (hiddata.size() > 0) {
                        try {
                            if (!sendable(hiddata)) {
                                continue;
                            }
                            mqttClient.publish(config.getMqtttopic(), new MqttMessage(new Gson().toJson(getAve(hiddata)).getBytes()));
                            hiddata.remove(hiddata.size() - 1);
                            hiddata.add(getAve(hiddata));
                            //System.out.println(hiddata.size());
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        timer.schedule(task, 0L, 500L);
    }

    private boolean sendable(CopyOnWriteArrayList<L> hiddata) {
        return true;
    }


    private   L getAve(CopyOnWriteArrayList<L> arr) {
        double sx = 0, sy = 0;
        String CID = "";
        int k = 0;
        for (int i = 0; i < arr.size(); i++) {
            //sx+=arr.get(i).x*(i+1);sy+=arr.get(i).y*(i+1);
            if (Double.isNaN(arr.get(i).x) || Double.isInfinite(arr.get(i).x)) {
                arr.remove(i);
            }
            sx += arr.get(i).x;
            sy += arr.get(i).y;
            CID = arr.get(i).CID;
        }
        //L res=new L(CID,sx/GetHei(arr.size()),sy/GetHei(arr.size()));
        L res = new L(CID, sx / (arr.size() - k), sy / (arr.size() - k));
        return res;
    }

    private   int getHei(int num) {
        int s = 0;
        for (int i = 1; i <= num; i++) {
            s += i;
        }
        return s;
    }
}