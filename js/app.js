'use strict';

/* App Module */

var phonecatApp = angular.module('phonecatApp', [
  'ngRoute',
  'phonecatControllers',
  'uiGmapgoogle-maps'
]);

phonecatApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/topics/', {
        templateUrl: 'partials/topic-list.html',
        controller: 'PhoneListCtrl'
      }).
      when('/comments/:topic', {
        templateUrl: 'partials/comments-list.html',
        controller: 'PhoneDetailCtrl'
      }).
      otherwise({
        redirectTo: '/topics'
      });
  }]);
