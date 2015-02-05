'use strict';

/* App Module */

var phonecatApp = angular.module('phonecatApp', [
  'ngRoute',
  'phonecatControllers',
  'uiGmapgoogle-maps',
  'hc.marked'
]);

phonecatApp.config(['markedProvider', function(markedProvider) {
  markedProvider.setOptions({gfm: true});
}]);

phonecatApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.
      when('/topics/', {
        templateUrl: 'partials/topic-list.html',
        controller: 'PhoneListCtrl'
      }).
      when('/comments/:topic?', {
        templateUrl: 'partials/comments-list.html',
        controller: 'PhoneDetailCtrl'
      }).
      when('/blog/', {
        templateUrl: 'partials/blog.html',
        controller: 'BlogCtrl'
      }).
      otherwise({
        redirectTo: '/comments'
      });
  }]);
