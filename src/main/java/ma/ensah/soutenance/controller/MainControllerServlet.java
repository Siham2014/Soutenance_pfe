package ma.ensah.soutenance.controller;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import ma.ensah.soutenance.model.dao.impl.*;
import ma.ensah.soutenance.model.dao.*;
import ma.ensah.soutenance.service.*;
import ma.ensah.soutenance.model.entity.*;
import ma.ensah.soutenance.service.impl.*;
import ma.ensah.soutenance.algorithm.*;
import ma.ensah.soutenance.algorithm.RepartitionStrategy;


/**
 * Servlet implementation class MainControllerServlet
 */
@WebServlet("/app")
public class MainControllerServlet extends HttpServlet {
	
    public MainControllerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        // DAO
        ProfesseurDao professeurDao = new ProfesseurDaoImpl();
        EtudiantDao etudiantDao = new EtudiantDaoImpl();
        GroupPfeDao groupPfeDao = new GroupPfeDaoImpl();

        // SERVICES
        ProfesseurService professeurService = new ProfesseurServiceImpl(professeurDao);
        EtudiantService etudiantService = new EtudiantServiceImpl(etudiantDao);

        if ("repartition".equals(action)) {

            // Algorithme avec seed
        	long seed = System.currentTimeMillis();
            RepartitionStrategy strategy = new SeededBalancedBlockFiliereStrategy(seed);

            // DAO version
            VersionRepartitionDao versionDao = new VersionRepartitionDaoImpl();

            // Service corrigé
            RepartitionEncadrantsService repartitionService =
                    new RepartitionEncadrantsServiceImpl(
                            professeurDao,
                            etudiantDao,
                            groupPfeDao,
                            versionDao,   
                            strategy
                    );

            repartitionService.repartirEncadrants();

            request.setAttribute("message", "Répartition effectuée !");
        }else if ("voirRepartition".equals(action)) {

        	String versionIdParam = request.getParameter("versionId");

        	List<GroupPfe> groupes;

        	if (versionIdParam != null && !versionIdParam.isEmpty()) {
        	    Long versionId = Long.parseLong(versionIdParam);
        	    groupes = groupPfeDao.findAllWithDetailsByVersion(versionId);
        	} else {
        	    groupes = groupPfeDao.findAllWithDetails();
        	}

       

            Map<Professeur, List<Etudiant>> repartition = new LinkedHashMap<>();

            for (GroupPfe g : groupes) {
                Professeur prof = g.getEncadrant();

                if (!repartition.containsKey(prof)) {
                    repartition.put(prof, new ArrayList<Etudiant>());
                }

                repartition.get(prof).addAll(g.getEtudiants());
            }

            request.setAttribute("repartition", repartition);

            request.getRequestDispatcher("/WEB-INF/views/repartition.jsp")
                   .forward(request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/accueil.jsp")
               .forward(request, response);
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
