'use strict';

angular.module('clarityApp')
  .controller('MainCtrl', function ($scope, $location, $http) {
    $scope.loadPresentation = function () {
      var data = {
        driveurl: $scope.driveUrl
      };
      $http({
        url: '/api/presentation',
        method: 'POST',
        data: JSON.stringify(data)
      }).success(function (data, status, headers, config) {
        console.log(data);
        var drive_id = data.drive_id;
        var token = data.presenter_id;
        var presentationId = data.presentation_id;
        $location.path('/presentation/' + presentationId);
      }).error(function (data, status, headers, config) {
        alert('Error!');
        console.log(data);
        // TODO: Do proper error displaying over here
      });
    };
  });
