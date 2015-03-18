/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.deri.iris.parser.node;

import org.deri.iris.parser.analysis.Analysis;

@SuppressWarnings("nls")
public final class ADirname extends PDirname
{
    private TTDash _tDash_;
    private TTId _tId_;
    private PParamlist _paramlist_;

    public ADirname()
    {
        // Constructor
    }

    public ADirname(
        @SuppressWarnings("hiding") TTDash _tDash_,
        @SuppressWarnings("hiding") TTId _tId_,
        @SuppressWarnings("hiding") PParamlist _paramlist_)
    {
        // Constructor
        setTDash(_tDash_);

        setTId(_tId_);

        setParamlist(_paramlist_);

    }

    @Override
    public Object clone()
    {
        return new ADirname(
            cloneNode(this._tDash_),
            cloneNode(this._tId_),
            cloneNode(this._paramlist_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseADirname(this);
    }

    public TTDash getTDash()
    {
        return this._tDash_;
    }

    public void setTDash(TTDash node)
    {
        if(this._tDash_ != null)
        {
            this._tDash_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tDash_ = node;
    }

    public TTId getTId()
    {
        return this._tId_;
    }

    public void setTId(TTId node)
    {
        if(this._tId_ != null)
        {
            this._tId_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tId_ = node;
    }

    public PParamlist getParamlist()
    {
        return this._paramlist_;
    }

    public void setParamlist(PParamlist node)
    {
        if(this._paramlist_ != null)
        {
            this._paramlist_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._paramlist_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._tDash_)
            + toString(this._tId_)
            + toString(this._paramlist_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._tDash_ == child)
        {
            this._tDash_ = null;
            return;
        }

        if(this._tId_ == child)
        {
            this._tId_ = null;
            return;
        }

        if(this._paramlist_ == child)
        {
            this._paramlist_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._tDash_ == oldChild)
        {
            setTDash((TTDash) newChild);
            return;
        }

        if(this._tId_ == oldChild)
        {
            setTId((TTId) newChild);
            return;
        }

        if(this._paramlist_ == oldChild)
        {
            setParamlist((PParamlist) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}