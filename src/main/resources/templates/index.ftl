<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>CSC 375 Assignment 3</title>

        <!-- Bootstrap CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">
        <!-- Custom CSS -->
        <link href="css/style.css" rel="stylesheet">
        <!-- Custom Fonts -->
        <link rel="stylesheet" href="css/hack.min.css">
    </head>
    <body>
        <div class="container-fluid">
            <div class="row">
                <div class="col-md-9" id="heatMapRow">
                    <canvas id="alloy" width="${data.getAlloyWidth()?c}" height="${data.getAlloyHeight()?c}"></canvas>
                </div>
                <div class="col-md-3">
                    <h3 id="heading">CSC 375 Assignment 3: HeatMap</h3>
                    </br>
                    <p>Max Iterations: ${data.getMaxIterations()}</p>
                    <p id="currentIteration">Current Iteration: 0</p>
                    </br>
                    <p>Convergence Threshold: ${data.getConvergenceThreshold()}</p>
                    <p id="currentTemperatureDifference">Current Highest Convergence: 0</p>
                    </br>
                    <p>Current Resolution: ${data.getAlloyWidth()?c}x${data.getAlloyHeight()?c}</p>
                    <p>Top Left Temperature: ${data.getTopLeftTemperature()}</p>
                    <p>Bottom Right Temperature: ${data.getBottomRightTemperature()}</p>
                    </br>
                    <h3 id="colorScale">Color Scale</h3>
                    <#list data.getHeatScale().getScale() as scale>
                        <div class="innerContainer" id="color${scale.getId()}" style="background-color:${scale.getColor()};"></div><div><h4>${scale.getStart()?c} - ${scale.getEnd()?c}</h4></div>
                    </#list>
                </div>
            </div>
        </div>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="js/jquery.min.js"></script>
        <!-- Bootstrap Javascript -->
        <script src="js/bootstrap.min.js"></script>
        <!-- Javascript for the alloy -->
        <script>
            var socket = new WebSocket("ws://" + window.location.host + "/socket");
            var canvas = document.getElementById('alloy');
            var context = canvas.getContext('2d');
            var squareSize = 1;
            var desiredWidth = ${data.getAlloyWidth()?c};
            var actualWidth = $("#heatMapRow").width();
            var scaleValue = actualWidth / desiredWidth;

            // Method to fill a square portion of the alloy
            function fillSquare(color, x, y){
                context.fillStyle = color;
                context.fillRect(x,y,squareSize,squareSize);
            }

            // Ran when a socket message is received, colors in the sent over pixels
            socket.onmessage = function(msg){
                var message = JSON.parse(msg.data);
                document.getElementById("currentIteration").innerHTML = "Current Iteration: " + message[1];
                document.getElementById("currentTemperatureDifference").innerHTML = "Current Highest Convergence: " + message[2];
                message[0].forEach(function(cell) {
                    fillSquare(document.getElementById("color"+cell[2]).style.backgroundColor, cell[0], cell[1]);
                });
            };

            // Start the alloy generation
            function startHeatMap() {
                // Calculate the scale value
                if(scaleValue >= 1) {
                    scaleValue = Math.floor(scaleValue);
                }
                actualWidth = desiredWidth * scaleValue;

                // Set the alloy canvas size
                canvas.width=actualWidth;
                canvas.height=actualWidth/2;
                canvas.style.width=canvas.width;
                canvas.style.height=canvas.height;

                // Fill the alloy canvas with the coldest color
                context.fillStyle = "${data.getHeatScale().getScale()[0].getColor()}";
                context.fillRect(0,0,canvas.width,canvas.height);
                context.scale(scaleValue,scaleValue);
            }

            // Start the alloy
            startHeatMap();
        </script>
    </body>
</html>