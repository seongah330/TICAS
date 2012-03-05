/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.umn.natsrl.sfim.comm.dmsxml;

/**
 * DMS XML Element Enumeration
 * @author Chongmyung Park
 */
public enum DXElement {
    // root tag
    DMSXML("DmsXml"),
    
    // request tag
    REQ_GET_CONFIG("GetDmsConfigReqMsg"),
    REQ_GET_STATUS("StatusReqMsg"),
    REQ_SET_MSG("SetSnglPgReqMsg"),
    
    // response tag
    RSP_GET_CONFIG("GetDmsConfigRespMsg"),
    RSP_GET_STATUS("StatusRespMsg"),    
    RSP_SET_MSG("SetSnglPgRspMsg"),
    
    // common tag
    M_ID("Id"),  
    M_ADDRESS("Address"),        
    M_IS_VALID("IsValid"),    
    M_ERR_MSG("ErrMsg"),    
    
    // GetDmsConfig's member tags
    M_SIGN_ACCESS("signAccess"),    
    M_MODEL("model"),    
    M_MAKE("make"),    
    M_VERSION("version"),    
    M_TYPE("type"),    
    M_H_BORDER("horizBorder"),    
    M_V_BORDER("vertBorder"),    
    M_H_PITCH("horizPitch"),    
    M_V_PITCH("vertPitch"),    
    M_SIGN_HEIGHT("signHeight"),    
    M_SIGN_WIDTH("signWidth"),    
    M_CHAR_HEIGHT_PIXEL("characterHeightPixels"),    
    M_CHAR_WIDTH_PIXEL("characterWidthPixels"),    
    M_SIGN_HEIGHT_PIXEL("signHeightPixels"),    
    M_SIGN_WIDTH_PIXEL("signWidthPixels"),    

    // GetStatusReq's member tags
    M_MSG_AVAILABLE("MsgTextAvailable"),    
    M_MSG_TXT("MsgText"),    
    M_ACT_PRIORITY("ActPriority"),    
    M_RUN_PRIORITY("RunPriority"),    
    M_OWNER("Owner"),    
    M_USE_ON_TIME("UseOnTime"),    
    M_ON_TIME("OnTime"),    
    M_USE_OFF_TIME("UseOffTime"),    
    M_OFF_TIME("OffTime"),    
    M_DISPLAY_TIME("DisplayTimeMS"),    
    M_USE_BITMAP("UseBitmap"),    
    M_BITMAP("Bitmap"), 
    ;
    
    
    public final String tag;

    private DXElement(String tag) {
        this.tag = tag;
    }
    
    @Override
    public String toString()
    {
        return tag;
    }
    
}
