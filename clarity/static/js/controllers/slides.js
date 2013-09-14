'use strict';

angular.module('clarityApp')
  .controller('SlideCtrl', function ($scope, slideService) {
      $scope.setSlides = slideService.setSlides;
      $scope.getSlides = slideService.getSlides;
    }
  );
