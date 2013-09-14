'use strict';

angular.module('clarityApp')
  .controller('MainCtrl', function ($scope, $location, $http) {
    $scope.loadPresentation = function () {
      var data = {
        driveurl: $scope.driveUrl
      };
      $http({
        url: '/api/create',
        method: 'POST',
        data: JSON.stringify(data)
      }).success(function (data, status, headers, config) {
        var presentation_id = data.id;
        var token = data.token;
        $location.path('/slides');
      }).error(function (data, status, headers, config) {
        console.log('Error!');
        console.log(data);
      });
    };
  });
