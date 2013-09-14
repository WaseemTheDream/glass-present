'use strict';

angular.module('clarityApp')
  .controller('PresentationCtrl', function ($scope, $routeParams, $http) {
    console.log('Initializing presentation controller');
    $scope.presentationId = $routeParams.presentationId;

    // Get the presenter id and slides from the server
    $http({
      url: '/api/presentation/' + $scope.presentationId,
      method: 'GET',
    }).success(function (data, status, headers, config) {
      console.log(data);
    }).error(function (data, status, headers, config) {
      alert('Error!');
      console.log(data);
      // TODO: Do proper error displaying over here
    });

  });