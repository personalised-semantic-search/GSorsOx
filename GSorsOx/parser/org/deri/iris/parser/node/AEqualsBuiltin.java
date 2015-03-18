/* This file was generated by SableCC (http://www.sablecc.org/). */

package org.deri.iris.parser.node;

import org.deri.iris.parser.analysis.Analysis;

@SuppressWarnings("nls")
public final class AEqualsBuiltin extends PBuiltin
{
    private PTerm _first_;
    private TTEq _tEq_;
    private PTerm _second_;

    public AEqualsBuiltin()
    {
        // Constructor
    }

    public AEqualsBuiltin(
        @SuppressWarnings("hiding") PTerm _first_,
        @SuppressWarnings("hiding") TTEq _tEq_,
        @SuppressWarnings("hiding") PTerm _second_)
    {
        // Constructor
        setFirst(_first_);

        setTEq(_tEq_);

        setSecond(_second_);

    }

    @Override
    public Object clone()
    {
        return new AEqualsBuiltin(
            cloneNode(this._first_),
            cloneNode(this._tEq_),
            cloneNode(this._second_));
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseAEqualsBuiltin(this);
    }

    public PTerm getFirst()
    {
        return this._first_;
    }

    public void setFirst(PTerm node)
    {
        if(this._first_ != null)
        {
            this._first_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._first_ = node;
    }

    public TTEq getTEq()
    {
        return this._tEq_;
    }

    public void setTEq(TTEq node)
    {
        if(this._tEq_ != null)
        {
            this._tEq_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._tEq_ = node;
    }

    public PTerm getSecond()
    {
        return this._second_;
    }

    public void setSecond(PTerm node)
    {
        if(this._second_ != null)
        {
            this._second_.parent(null);
        }

        if(node != null)
        {
            if(node.parent() != null)
            {
                node.parent().removeChild(node);
            }

            node.parent(this);
        }

        this._second_ = node;
    }

    @Override
    public String toString()
    {
        return ""
            + toString(this._first_)
            + toString(this._tEq_)
            + toString(this._second_);
    }

    @Override
    void removeChild(@SuppressWarnings("unused") Node child)
    {
        // Remove child
        if(this._first_ == child)
        {
            this._first_ = null;
            return;
        }

        if(this._tEq_ == child)
        {
            this._tEq_ = null;
            return;
        }

        if(this._second_ == child)
        {
            this._second_ = null;
            return;
        }

        throw new RuntimeException("Not a child.");
    }

    @Override
    void replaceChild(@SuppressWarnings("unused") Node oldChild, @SuppressWarnings("unused") Node newChild)
    {
        // Replace child
        if(this._first_ == oldChild)
        {
            setFirst((PTerm) newChild);
            return;
        }

        if(this._tEq_ == oldChild)
        {
            setTEq((TTEq) newChild);
            return;
        }

        if(this._second_ == oldChild)
        {
            setSecond((PTerm) newChild);
            return;
        }

        throw new RuntimeException("Not a child.");
    }
}