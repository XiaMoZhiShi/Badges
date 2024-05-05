package xiamomc.badges.storage.badge;

import com.google.gson.annotations.Expose;

public class StoredBadge
{
    @Expose
    public String name = "~UNSET~";

    @Expose
    public String identifier = "unset";

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof String string)
        {
            return this.identifier.equalsIgnoreCase(string);
        }
        return super.equals(obj);
    }

    public StoredBadge()
    {
    }

    public StoredBadge(String name, String id)
    {
        this.name = name;
        this.identifier = id;
    }

    @Override
    protected StoredBadge clone()
    {
        return new StoredBadge(this.name, this.identifier);
    }
}
