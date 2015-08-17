/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.publicationmetasearchengine.data;

/**
 *
 * @author Kamciak
 */

public class Journal {
    private Integer id;
    private final String  title;
    private final String  ISSN;
    private final Float   impactFactor2013_2014;
    private final Float   impactFactor2012;
    private final Float   impactFactor2011;
    private final Float   impactFactor2010;
    private final Float   impactFactor2009;
    private final Float   impactFactor2008;
    
    public Journal(Integer id,
                   String title, 
                   String ISSN, 
                   Float impactFactor2013_2014,
                   Float impactFactor2012,
                   Float impactFactor2011,
                   Float impactFactor2010,
                   Float impactFactor2009,
                   Float impactFactor2008)
    {
        this.id = id;
        this.title = title;
        this.ISSN = ISSN;
        this.impactFactor2013_2014 = impactFactor2013_2014;
        this.impactFactor2012 = impactFactor2012;
        this.impactFactor2011 = impactFactor2011;
        this.impactFactor2010 = impactFactor2010;
        this.impactFactor2009 = impactFactor2009;
        this.impactFactor2008 = impactFactor2008;
    }
    
    public void setId(Integer id)
    {
        this.id = id;
    }
    
    @Override
    public String toString()
    {
        return title + " " + ISSN;
    }
    
    public String getTitle() {
        return title;
    }

    public String getISSN() {
        return ISSN;
    }

    public Float getImpactFactor2013_2014() {
        return impactFactor2013_2014;
    }

    public Float getImpactFactor2012() {
        return impactFactor2012;
    }

    public Float getImpactFactor2011() {
        return impactFactor2011;
    }

    public Float getImpactFactor2010() {
        return impactFactor2010;
    }

    public Float getImpactFactor2009() {
        return impactFactor2009;
    }

    public Float getImpactFactor2008() {
        return impactFactor2008;
    }
    
}
