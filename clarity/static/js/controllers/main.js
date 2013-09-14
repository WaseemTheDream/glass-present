'use strict';

angular.module('clarityApp')
  .controller('MainCtrl', function ($scope, $http) {
    $scope.loadPresentation = function () {
      var data = {
        driveurl: $scope.driveUrl
      };
      $http({
        url: '/api/create',
        method: 'POST',
        data: JSON.stringify(data)
      }).success(function (data, status, headers, config) {
        console.log('Success!');
        console.log(data);
      }).error(function (data, status, headers, config) {
        console.log('Error!');
        console.log(data);
      });
    };
  });