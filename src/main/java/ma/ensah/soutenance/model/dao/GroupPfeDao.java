package ma.ensah.soutenance.model.dao;

import java.util.List;

import ma.ensah.soutenance.model.entity.GroupPfe;

public interface GroupPfeDao {
    void save(GroupPfe groupPfe);
    List<GroupPfe> findAllWithDetails() ;
    List<GroupPfe> findAllWithDetailsByVersion(Long versionId);
    void resetDatabase();
}