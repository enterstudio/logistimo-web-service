var fs = require('fs');
module.exports = {
    locations: JSON.parse(fs.readFileSync('../locations.json', 'utf8')),
    printCountries: function(){
        for(var country in this.locations.data){
            if (this.locations.data.hasOwnProperty(country)) {
                console.log(this.locations.data[country].name+"("+country+")");
                this.printStates(country);
            }
        }
    },
    printStates: function(country){
        if(this.locations.data.hasOwnProperty(country)){
            var cData = this.locations.data[country];
            var cName = cData.name+"("+country+"),";
            for(var state in cData.states){
                if(cData.states.hasOwnProperty(state)){
                    var sName = cName+state+",";
                    var sData = cData.states[state];
                    if(sData.hasOwnProperty("districts")){
                        for(var district in sData.districts){
                            if(sData.districts.hasOwnProperty(district)){
                                console.log(sName+district);
                            }
                        }
                    }
                }
            }
        }
    },
    printDistricts: function(country, stateX){
    if(this.locations.data.hasOwnProperty(country)){
        var cData = this.locations.data[country];
        var cName = cData.name+"("+country+"),";
        for(var state in cData.states){
            if(cData.states.hasOwnProperty(state) && stateX == state ){
                var sName = cName+state+",";
                var sData = cData.states[state];
                if(sData.hasOwnProperty("districts")){
                    for(var district in sData.districts){
                        if(sData.districts.hasOwnProperty(district)){
                            console.log(sName+district);
                        }
                    }
                }
            }
        }
    }
}

};