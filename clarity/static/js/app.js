'use strict';

angular.module('clarityApp', [])
	.config(function ($routeProvider) {
		$routeProvider
			.when('/', {
				templateUrl: 'static/views/select-presentation.html',
				controller: 'MainCtrl'
			})
			.when('/present', {
				templateUrl: 'static/views/present.html',
				controller: 'PresentCtrl'
			})
      .when('/slides', {
        templateUrl: 'static/views/slides.html',
        controller: 'SlideCtrl'
      })
			.otherwise({
				redirectTo: '/'
			});
	});
