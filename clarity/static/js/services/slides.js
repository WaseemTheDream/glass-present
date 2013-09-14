'use strict';

angular.module('clarityApp')
  .service('slideService', function () {
    var slides = [];
    return {
      setSlides: function(slideList) {
        slides = slideList;
      },

      getSlides: function() {
        return slides;
      }
    };
  });
