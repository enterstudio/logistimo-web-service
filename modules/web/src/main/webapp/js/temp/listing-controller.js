var listingController = angular.module('listingController', []);

/**
 * Listing Controller is a base helper Controller for all listable/tabular views in Logistimo
 *  eg: Materials, Inventory, Transactions, Orders
 *  It provides pagination functions, sort, automatic url update on fitlers, and sort by functions
 *  It assumes the scope includes certain parameters
 *  offset - current page offset
 *  numFound
 *  size - page size
 *  init() - to reset  params
 *  fetch() - function to call to fetch on pagination event
 *  wparams - [optional] array of pairs [ param-used-in-url, internal-param-name-to-watch ] eg: [["tag","tag"],["search","search.mnm"]];
 *
 *
 * @param $scope
 * @param requestContext
 * @param $location
 */
