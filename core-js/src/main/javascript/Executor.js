/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
 */

/**
 * I need this class to clean up things after loading has finished. Without this, async functions at startup are a
 * nightmare.
 */

core.Executor = new Class({
    options:{
       isLoading:true
    },
    initialize:function(options){
        this._pendingFunctions=[];
    },
    setLoading:function(isLoading){
        this.options.isLoading = isLoading;
        if(!isLoading){
            this._pendingFunctions.forEach(function(item){
                var result = item.fn.attempt(item.args, item.bind);
                core.assert(result!=false, "execution failed");
            });
            this._pendingFunctions=[];
        }
    },
    isLoading:function(){
        return this.options.isLoading;
    },
    delay:function(fn, delay, bind, args){
        if(this.options.isLoading){
            this._pendingFunctions.push({fn:fn, bind:bind, args:args});
        }
        else{
            fn.delay(delay, bind, args);
        }
    }

});

core.Executor.instance = new core.Executor();