'use strict';

angular.module('clarityApp')
  .controller('PresentationCtrl', function ($scope, $routeParams, $http, $q) {
    console.log('Initializing presentation controller');
    $scope.presentationId = $routeParams.presentationId;
    $scope.presenterId = null;
    $scope.token = null;
    $scope.slides = [];
    $scope.pageIdToSlide = {};
    $scope.currentSlide = {};
    $scope.presentationStarted = false;

    $scope.nextSlide = function () {
      var index = $scope.slides.indexOf($scope.currentSlide);
      if (-1 < index && index < $scope.slides.length - 1) {
        $scope.currentSlide = $scope.slides[++index];
      }
    }

    $scope.previousSlide = function () {
      var index = $scope.slides.indexOf($scope.currentSlide);
      if (0 < index && index < $scope.slides.length) {
        $scope.currentSlide = $scope.slides[--index];
      }
    }

    $scope.gotoSlide = function (index) {
      if (-1 < index && index < $scope.slides.length) {
        $scope.currentSlide = $scope.slides[index];
      }
    }

    $scope.getSlides = function () {
      var d = $q.defer();
      // Get the presenter id and slides from the server
      $http({
        url: '/api/presentation/' + $scope.presentationId,
        method: 'GET',
      }).success(function (data, status, headers, config) {
        d.resolve(data);
      }).error(function (data, status, headers, config) {
        d.reject(status);
      });
      return d.promise;
    }

    // For debugging without Google Glass
    $scope.controllerRoundtrip = function () {
      $.ajax({
        url: '/api/controller',
        type: 'POST', 
        data: {
          presenter_id: $scope.presenterId,
          presentation_id: $scope.presentationId,
          page_id: $scope.pageId
        },
        success: function (data) {
          console.log(data);
        }
      });
    }

    $scope.connectRoundtrip = function () {
      $.ajax({
        url: '/api/controller/' + $scope.presentationId + "?presenter_id=" + $scope.presenterId,
        type: 'GET',
        success: function (data) {
          console.log(data);
        }
      });
    };

    $scope.fullScreen = function () {
      if (screenfull.enabled) {
        screenfull.request($('#slide-container img').get(0));
      }
    };

    var connected = false;
    $scope.channel = {
      onopen: function () {
        console.log('channel connection established');
      },
      onmessage: function (message) {
        console.log('received channel message');
        var data = JSON.parse(message.data)

        if (data.event === 'glass connected') {
          // Hide the QR code and show the slide if not connected yet
          if (!connected) {
            $('#qr-container').fadeTo('fast', 0, function() {
              var $play_button = $('#play-button');
              var $parent = $play_button.parent();
              $play_button.css('position', 'absolute');
              $play_button.css('top', '8em');

              $("#play-button").fadeIn('slow');
            });
            connected = true;
          }

        } else if (data.event === 'slide changed') {
          if ($scope.pageIdToSlide[data.page_id]) {
            console.log('Changing slide to page_id ' + data.page_id);
            $scope.currentSlide = $scope.pageIdToSlide[data.page_id];
            $scope.$apply()
          } else {
            console.log('Invalid page_id ' + data.page_id + ' was sent.');
          }

        } else {
          console.log('Invalid event type');
        }

      },
      onerror: function (data) {
        alert('onError');
        console.log(data);
      },
      onclose: function () {
        console.log('channel connection closed');
      }
    };

    $scope.getSlides().then(function (data) {
      // Save the slides data and open the first one
      angular.forEach(data.slides, function (value, key) {
        if (key == 0) $scope.currentSlide = value;
        $scope.slides.push(value);
        $scope.pageIdToSlide[value.page_id] = value;
      });
      $scope.presenterId = data.presenter_id;
      $scope.token = data.token;

      console.log(data);
      console.log('socket:' + $scope.token);

      // Establish a channel with the server
      var channel = new goog.appengine.Channel($scope.token);
      var socket = channel.open();
      socket.onopen = $scope.channel.onopen;
      socket.onmessage = $scope.channel.onmessage
      socket.onerror = $scope.channel.onerror
      socket.onclose = $scope.channel.onclose
    }, function (error) {
      // TODO: Handle errors properly
      alert(error);
    });

  });
