
package edu.umn.natsrl.vissimcom.wrapper_53_student;

import org.jawin.*;
import org.jawin.constants.*;
import org.jawin.marshal.*;
import org.jawin.io.*;
import java.io.*;
import java.util.Date;

/**
 * Jawin generated code please do not edit
 *
 * Dispatch: IStaticObjects
 * Description: Interface to access static 3D object collections.
 * Help file:   
 *
 * @author JawinTypeBrowser
 */

public class IStaticObjects extends DispatchPtr {
	public static final GUID DIID = new GUID("{c1897544-e7f4-401d-95E2-4CF184E7F0EC}");
	public static final int IID_TOKEN;
	static {
		IID_TOKEN = IdentityManager.registerProxy(DIID, IStaticObjects.class);
	}

	/**
	 * The required public no arg constructor.
	 * <br><br>
	 * <b>Important:</b>Should never be used as this creates an uninitialized
	 * IStaticObjects (it is required by Jawin for some internal working though).
	 */
	public IStaticObjects() {
		super();
	}

	/**
	 * For creating a new COM-object with the given progid and with 
	 * the IStaticObjects interface.
	 * 
	 * @param progid the progid of the COM-object to create.
	 */
	public IStaticObjects(String progid) throws COMException {
		super(progid, DIID);
	}

	/**
	 * For creating a new COM-object with the given clsid and with 
	 * the IStaticObjects interface.
	 * 
	 * @param clsid the GUID of the COM-object to create.
	 */
	public IStaticObjects(GUID clsid) throws COMException {
		super(clsid, DIID);
	}

	/**
	 * For getting the IStaticObjects interface on an existing COM-object.
	 * This is an alternative to calling {@link #queryInterface(Class)}
	 * on comObject.
	 * 
	 * @param comObject the COM-object to get the IStaticObjects interface on.
	 */
	public IStaticObjects(COMPtr comObject) throws COMException {
		super(comObject);
	}

	public int getIIDToken() {
		return IID_TOKEN;
	}
	
	
    /**
    *
    
    * @return void
    **/
    /*public void QueryInterface(Object[] riid,void[] 
        [] ppvObj) throws COMException
    {
      
		invokeN("QueryInterface", new Object[] {riid, ppvObj});
        
    }*/
    /**
    *
    
    * @return int
    **/
    /*public int AddRef() throws COMException
    {
      
		return ((Integer)invokeN("AddRef", new Object[] {})).intValue();
        
    }*/
    /**
    *
    
    * @return int
    **/
    /*public int Release() throws COMException
    {
      
		return ((Integer)invokeN("Release", new Object[] {})).intValue();
        
    }*/
    /**
    *
    
    * @return void
    **/
    /*public void GetTypeInfoCount(int[] pctinfo) throws COMException
    {
      
		invokeN("GetTypeInfoCount", new Object[] {pctinfo});
        
    }*/
    /**
    *
    
    * @param itinfo
    * @param lcid
    * @return void
    **/
    /*public void GetTypeInfo(int itinfo,int lcid,void[] 
        [] pptinfo) throws COMException
    {
      
		invokeN("GetTypeInfo", new Object[] {new Integer(itinfo), new Integer(lcid), pptinfo});
        
    }*/
    /**
    *
    
    * @param cNames
    * @param lcid
    * @return void
    **/
    /*public void GetIDsOfNames(Object[] riid,int[] 
        [] rgszNames,int cNames,int lcid,int[] rgdispid) throws COMException
    {
      
		invokeN("GetIDsOfNames", new Object[] {riid, rgszNames, new Integer(cNames), new Integer(lcid), rgdispid});
        
    }*/
    /**
    *
    
    * @param dispidMember
    * @param lcid
    * @param wFlags
    * @return void
    **/
    /*public void Invoke(int dispidMember,Object[] riid,int lcid,short wFlags,Object[] pdispparams,Variant[] pvarResult,Object[] pexcepinfo,int[] puArgErr) throws COMException
    {
      
		invokeN("Invoke", new Object[] {new Integer(dispidMember), riid, new Integer(lcid), new Short(wFlags), pdispparams, pvarResult, pexcepinfo, puArgErr});
        
    }*/
    /**
    *
    * @return IUnknown
    **/
    public IUnknown get_NewEnum() throws COMException
    {
         return (IUnknown)get("_NewEnum");
    }
        
    /**
    *
    * @return void
    **/
    public IStaticObject getItem(Variant index) throws COMException
    {
        IStaticObject res = new IStaticObject();
          DispatchPtr dispPtr = (DispatchPtr)get("Item", index);
          res.stealUnknown(dispPtr);
          return res;
    }
        
    /**
    *
    * @return int
    **/
    public int getCount() throws COMException
    {
        return ((Integer)get("Count")).intValue();
    }
        
    /**
    *
    
    * @param Name
    * @param vWorldPoint
    * @return void
    **/
    public IStaticObject GetStaticObjectByName(String Name,Variant vWorldPoint) throws COMException
    {
      IStaticObject res = new IStaticObject();
          DispatchPtr dispPtr = (DispatchPtr)invokeN("GetStaticObjectByName", new Object[] {Name, vWorldPoint});
          res.stealUnknown(dispPtr);
          return res;
        
    }
    /**
    *
    
    * @param vName
    * @return void
    **/
    public IStaticObject GetStaticObjectByCoord(Object[] pWorldPoint,Variant vName) throws COMException
    {
      IStaticObject res = new IStaticObject();
          DispatchPtr dispPtr = (DispatchPtr)invokeN("GetStaticObjectByCoord", new Object[] {pWorldPoint, vName});
          res.stealUnknown(dispPtr);
          return res;
        
    }
}
