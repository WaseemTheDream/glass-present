'use strict';

angular.module('clarityApp', [])
	.config(function ($routeProvider) {
		$routeProvider
			.when('/', {
				templateUrl: 'static/views/select-presentation.html',
				controller: 'MainCtrl'
			})
			.when('/presentation', {
				templateUrl: 'static/views/presentation.html',
				controller: 'PresentationCtrl'
			})
			.otherwise({
				redirectTo: '/'
			});
	});
