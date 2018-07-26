import {HttpClient, HttpClientModule} from "@angular/common/http";
import {FactoryProvider, NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from "@angular/core";
import {FlexLayoutModule} from "@angular/flex-layout";
import {BrowserModule} from "@angular/platform-browser";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {UpgradeModule} from "@angular/upgrade/static";
import {CovalentLoadingModule} from "@covalent/core/loading";
import {TranslateLoader, TranslateModule, TranslateModuleConfig, TranslateService} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {UIRouterModule} from "@uirouter/angular";
import {UIRouterUpgradeModule} from "@uirouter/angular-hybrid";

import "routes"; // load AngularJS application
import {KyloCommonModule} from "./common/common.module";
import {KyloServicesModule} from "./services/services.module";
import {CategoriesModule} from "./feed-mgr/categories/categories.module";
import { SLAModule } from "./feed-mgr/sla/sla.module";
import { AuthModule } from "./auth/auth.module";
import { DataSourcesModule } from "./feed-mgr/datasources/datasources.module";
import { TemplateModule } from "./feed-mgr/templates/templates.module";

export function translateHttpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http, "locales/", ".json");
}

const translateConfig: TranslateModuleConfig = {
    loader: {
        provide: TranslateLoader,
        useFactory: translateHttpLoaderFactory,
        deps: [HttpClient]
    }
};

@NgModule({
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        CovalentLoadingModule,
        FlexLayoutModule,
        HttpClientModule,
        KyloCommonModule,
        KyloServicesModule,
        TranslateModule.forRoot(translateConfig),
        CategoriesModule,
        SLAModule,
        TemplateModule,
        UIRouterModule,
        UIRouterUpgradeModule,
        UpgradeModule,
        AuthModule,
        DataSourcesModule
    ],
    providers: [
        {provide: "$ocLazyLoad", useFactory: (i: any) => i.get("$ocLazyLoad"), deps: ["$injector"]} as FactoryProvider,
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ]
})
export class KyloModule {

    constructor(translate: TranslateService) {
        translate.setDefaultLang("en");
    }

    ngDoBootstrap() {
        // needed by UpgradeModule
    }
}
