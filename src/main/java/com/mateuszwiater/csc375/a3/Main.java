package com.mateuszwiater.csc375.a3;

import com.mateuszwiater.csc375.a3.simulator.ClientSimulator;
import com.mateuszwiater.csc375.a3.simulator.ClusterSimulator;
import com.mateuszwiater.csc375.a3.util.Global;
import com.mateuszwiater.csc375.a3.websocket.WebSocketManager;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    Global global;

    volatile boolean startSimulation;

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        if(args.length != 0) {
            //Start the client
            System.out.println("Starting the client...");
            (new ClientSimulator(args[1], Integer.parseInt(args[2]))).start();
        } else {
            args = new String[3];
            args[1] = "0"; //clients
            args[2] = "4566"; //port
            // Create the main object
            Main main = new Main();
            // Setup the needed data
            System.out.print("Setting up constants...");
            main.setup();
            // Configure the webservice
            System.out.print("Starting the webservice...");
            main.configureWebService();
            // Start heating the alloy
            main.heatAlloy(args);
        }
    }

    private void setup() {
        int maxIterations           = 1000000;
        int alloyHeight             = 500;

        int topLeftTemperature     = 1000000;
        int bottomRightTemperature = 800000;

        double convergenceThreshold = 0.001;
        double thermalConstant1     = 0.75; //0.75
        double thermalConstant2     = 1.0; //1.0
        double thermalConstant3     = 1.25; //1.25

        String[] colors = {
                "#3F48CC",
                "#2075DA",
                "#00A2E8",
                "#11AA9A",
                "#22B14C",
                "#91D226",
                "#FFF200",
                "#FFB914",
                "#FF7F27",
                "#F64E26",
                "#ED1C24",
                "#BB0E1D",
                "#880015"};

        startSimulation = false;
        global = new Global(maxIterations, alloyHeight, topLeftTemperature, bottomRightTemperature, convergenceThreshold, new double[]{thermalConstant1, thermalConstant2, thermalConstant3}, colors, Math.max(topLeftTemperature, bottomRightTemperature));
    }

    private void configureWebService() {
        // Create the WebSocket
        webSocket("/socket", WebSocketManager.class);

        // Set the static file location
        staticFileLocation("/public_html");

        // Set the route for the main page
        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("data", global);
            startSimulation = true;
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());
    }

    private void heatAlloy(String[] args) {
        // Create the cluster simulator
        System.out.println("Waiting for " + args[1] + " clients to connect...");
        ClusterSimulator simulator = new ClusterSimulator(Integer.parseInt(args[1]), Integer.parseInt(args[2]), global);
        // Wait for one person to connect to the GUI
        System.out.println("Waiting for GUI connections...");
        while(!startSimulation) {}
        System.out.println("Starting Simulation");
        simulator.start();
    }

    public static class FreeMarkerEngine extends TemplateEngine {
        private Configuration configuration;

        // Creates a FreeMarkerEngine
        public FreeMarkerEngine() {
            this.configuration = createDefaultConfiguration();
        }

        @Override
        public String render(ModelAndView modelAndView) {
            try {
                StringWriter stringWriter = new StringWriter();
                Template template = configuration.getTemplate(modelAndView.getViewName());
                template.process(modelAndView.getModel(), stringWriter);
                return stringWriter.toString();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            } catch (TemplateException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private Configuration createDefaultConfiguration() {
            Configuration configuration = new Configuration();
            configuration.setClassForTemplateLoading(FreeMarkerEngine.class, "");
            try {
                configuration.setDirectoryForTemplateLoading(new File(this.getClass().getClassLoader().getResource("templates").toURI()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return configuration;
        }
    }
}
