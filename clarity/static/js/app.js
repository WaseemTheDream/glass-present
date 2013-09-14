'use strict';

angular.module('clarityApp', [])
	.config(function ($routeProvider) {
		$routeProvider
			.when('/', {
				templateUrl: 'static/views/main.html',
				controller: 'MainCtrl'
			})
			.when('/presentation/:presentationId', {
				templateUrl: 'static/views/presentation.html',
				controller: 'PresentationCtrl'
			})
			.otherwise({
				redirectTo: '/'
			});
	});
