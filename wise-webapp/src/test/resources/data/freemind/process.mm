<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<map version="0.9.0">
    <node TEXT="California" ID="ID_1">
        <node TEXT="Northern California" POSITION="left" ID="ID_24">
            <node TEXT="Oakland/Berkeley" POSITION="left" ID="ID_28"/>
            <node TEXT="San Mateo" POSITION="left" ID="ID_27"/>
            <node TEXT="Other North" POSITION="left" ID="ID_31"/>
            <node TEXT="San Francisco" POSITION="left" ID="ID_29"/>
            <node TEXT="Santa Clara" POSITION="left" ID="ID_30"/>
            <node TEXT="Marin/Napa/Solano" POSITION="left" ID="ID_26"/>
        </node>
        <node TEXT="Hawaii" POSITION="left" ID="ID_2"/>
        <node TEXT="Southern California" POSITION="left" ID="ID_25">
            <node TEXT="Los Angeles" POSITION="left" ID="ID_33"/>
            <node TEXT="Anaheim/Santa Ana" POSITION="left" ID="ID_34"/>
            <node TEXT="Ventura" POSITION="left" ID="ID_32"/>
            <node TEXT="Other South" POSITION="left" ID="ID_35"/>
        </node>
        <node TEXT="Policy Bodies" POSITION="left" ID="ID_36">
            <node TEXT="Advocacy" POSITION="left" ID="ID_50">
                <node TEXT="AAO" POSITION="left" ID="ID_42"/>
                <node TEXT="ASCRS" POSITION="left" ID="ID_43"/>
                <node TEXT="EBAA" POSITION="left" ID="ID_41"/>
            </node>
            <node TEXT="Military" POSITION="left" ID="ID_47"/>
            <node TEXT="United Network for Organ Sharing" POSITION="left" ID="ID_40"/>
            <node TEXT="Kaiser Hospital System" POSITION="left" ID="ID_48"/>
            <node TEXT="University of California System" POSITION="left" ID="ID_49"/>
            <node TEXT="CMS" POSITION="left" ID="ID_44">
                <node TEXT="Medicare Part A" POSITION="left" ID="ID_45"/>
                <node TEXT="Medicare Part B" POSITION="left" ID="ID_46"/>
            </node>
        </node>
        <node TEXT="Corneal Tissue OPS" POSITION="right" ID="ID_51">
            <node TEXT="Transplant Bank International" POSITION="right" ID="ID_57">
                <node TEXT="Orange County Eye and Transplant Bank" POSITION="right" ID="ID_58"/>
                <node wORDER="1" wCOORDS="554,152" TEXT="Northern California Transplant Bank" STYLE="bubble"
                      POSITION="right" ID="ID_59" BACKGROUND_COLOR="#00ffd5">
                    <node wORDER="0" wCOORDS="815,143" TEXT="In 2010, 2,500 referrals forwarded to OneLegacy"
                          STYLE="elipse" POSITION="right" ID="ID_72"/>
                </node>
                <node wORDER="2" wCOORDS="577,179" TEXT="Doheny Eye and Tissue Transplant Bank" STYLE="bubble"
                      POSITION="right" LINK="http://www.dohenyeyebank.org/" ID="ID_56" BACKGROUND_COLOR="#00ffd5">
                    <hook NAME="accessories/plugins/NodeNote.properties">
                        <text>Part%20of%20Tissue%20Banks%20International</text>
                    </hook>
                </node>
                <arrowlink ENDARROW="Default" DESTINATION="ID_52"/>
                <arrowlink ENDARROW="Default" DESTINATION="ID_55"/>
            </node>
            <node wORDER="1" wCOORDS="289,209" TEXT="OneLegacy" STYLE="bubble" POSITION="right" ID="ID_52"
                  BACKGROUND_COLOR="#00ffd5">
                <hook NAME="accessories/plugins/NodeNote.properties">
                    <text>EIN%20953138799</text>
                </hook>
                <node wORDER="0" wCOORDS="454,200" TEXT="In 2010, 11,828 referrals" STYLE="elipse" POSITION="right"
                      ID="ID_60"/>
            </node>
            <node wORDER="2" wCOORDS="300,239" TEXT="San Diego Eye Bank" STYLE="bubble" POSITION="right" ID="ID_65"
                  BACKGROUND_COLOR="#00ffd5">
                <node wORDER="0" wCOORDS="474,230" TEXT="In 2010, 2,555 referrals" STYLE="elipse" POSITION="right"
                      ID="ID_67"/>
            </node>
            <node TEXT="California Transplant Donor Network" POSITION="right" ID="ID_55"/>
            <node TEXT="California Transplant Services" POSITION="right" ID="ID_70">
                <node wORDER="0" wCOORDS="508,293" TEXT="In 2010, 0 referrals" STYLE="elipse" POSITION="right"
                      ID="ID_71"/>
            </node>
            <node TEXT="Lifesharing" POSITION="right" ID="ID_54"/>
            <node TEXT="DCI Donor Services" POSITION="right" ID="ID_62">
                <node wORDER="0" wCOORDS="509,350" TEXT="Sierra Eye and Tissue Donor Services" STYLE="bubble"
                      POSITION="right" ID="ID_53" BACKGROUND_COLOR="#00ffd5">
                    <hook NAME="accessories/plugins/NodeNote.properties">
                        <text>EIN%20581990866</text>
                    </hook>
                    <node wORDER="0" wCOORDS="728,341" TEXT="In 2010, 2.023 referrals" STYLE="elipse" POSITION="right"
                          ID="ID_63"/>
                </node>
            </node>
            <node wORDER="7" wCOORDS="276,380" TEXT="SightLife" STYLE="bubble" POSITION="right" ID="ID_61"
                  BACKGROUND_COLOR="#00ffd5"/>
        </node>
        <node TEXT="Tools" POSITION="left" ID="ID_38">
            <node TEXT="Darthmouth Atlas of Health" POSITION="left" ID="ID_37"/>
            <node TEXT="HealthLandscape" POSITION="left" ID="ID_39"/>
        </node>
        <node TEXT="QE Medicare" POSITION="right" ID="ID_84"/>
        <node TEXT="CMS Data" POSITION="right" ID="ID_85"/>
        <node TEXT="Ambulatory Payment Classification" POSITION="right" ID="ID_3">
            <node TEXT="CPT's which don't allow V2785" POSITION="right" ID="ID_89">
                <node TEXT="Ocular Reconstruction Transplant" POSITION="right" ID="ID_77">
                    <node TEXT="65780 (amniotic membrane tranplant" POSITION="right" ID="ID_78"/>
                    <node TEXT="65781 (limbal stem cell allograft)" POSITION="right" ID="ID_79"/>
                    <node TEXT="65782 (limbal conjunctiva autograft)" POSITION="right" ID="ID_80"/>
                </node>
                <node TEXT="Endothelial keratoplasty" POSITION="right" ID="ID_12">
                    <node TEXT="65756" POSITION="right" ID="ID_23"/>
                </node>
                <node TEXT="Epikeratoplasty" POSITION="right" ID="ID_82">
                    <node TEXT="65767" POSITION="right" ID="ID_83"/>
                </node>
            </node>
            <node TEXT="Anterior lamellar keratoplasty" POSITION="right" ID="ID_8">
                <node TEXT="65710" POSITION="right" ID="ID_17"/>
            </node>
            <node wORDER="2" wCOORDS="557,-166" TEXT="Processing, preserving, and transporting corneal tissue"
                  STYLE="elipse" POSITION="right" ID="ID_86">
                <node TEXT="V2785" POSITION="right" ID="ID_87"/>
                <node wORDER="1" wCOORDS="810,-163" TEXT="Laser incision in recepient" STYLE="elipse" POSITION="right"
                      ID="ID_75">
                    <node TEXT="0290T" POSITION="right" ID="ID_76"/>
                </node>
            </node>
            <node wORDER="3" wCOORDS="488,-121" TEXT="Laser incision in donor" STYLE="elipse" POSITION="right"
                  ID="ID_73">
                <node TEXT="0289T" POSITION="right" ID="ID_74"/>
            </node>
            <node TEXT="Penetrating keratoplasty" POSITION="right" ID="ID_9">
                <node TEXT="65730 (in other)" POSITION="right" ID="ID_18"/>
                <node TEXT="65755 (in pseudoaphakia)" POSITION="right" ID="ID_20"/>
                <node TEXT="65750 (in aphakia)" POSITION="right" ID="ID_19"/>
            </node>
        </node>
    </node>
</map>