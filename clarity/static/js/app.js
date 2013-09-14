'use strict';

angular.module('clarityApp', [])
	.config(function ($routeProvider) {
		$routeProvider
			.when('/', {
				templateUrl: 'static/views/select-presentation.html',
				controller: 'MainCtrl'
			})
			.when('/', {
				templateUrl: 'static/views/present.html',
				controller: 'PresentCtrl'
			})
			.otherwise({
				redirectTo: '/'
			});
	});
