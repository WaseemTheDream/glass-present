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

    $scope.channel = {
      onopen: function () {
        alert('open');
      },
      onmessage: function () {
        alert('message');
      },
      onerror: function () {
        alert('onError');
      },
      onclose: function () {
        alert('close');
      }
    };

    $scope.getSlides().then(function(data) {
      // Save the slides data and open the first one
      angular.forEach(data.slides, function (value, key) {
        if (key == 0) $scope.currentSlide = value;
        $scope.slides.push(value);
        $scope.pageIdToSlide[value.page_id] = value;
      });
      $scope.presenterId = data.presenter_id;
      $scope.token = data.token;

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