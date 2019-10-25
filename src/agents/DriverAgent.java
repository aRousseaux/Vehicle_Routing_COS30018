
package agents;

import java.awt.Point;
import java.util.Arrays;
import java.util.logging.Logger;

import data.DataModel;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.tools.sniffer.Message;

public class DriverAgent extends Agent 
{
	private static final long serialVersionUID = 1L;
	
	private DataModel fDataModel;
	private int fIdentification;
	
	private int fLocationIndex;
	private Point fPosition;
	private int[] fRoute;
	
	private AMSAgentDescription[] fAgents;
	private AMSAgentDescription fDBAgent;
	private AMSAgentDescription fMasterAgent;

	private static final Logger fLogger = Logger.getGlobal();
	
	protected void setup()
	{
		fLogger.info("Agent " + getLocalName() + " started.");

		Object[] args = getArguments();

		fDataModel = (DataModel) args[0];
		fIdentification = (int) args[1];
		fPosition = null;

		findAgents();

		addBehaviour(new CyclicBehaviour(this)
		{
			private static final long serialVersionUID = 1L;

			public void action()
			{
				ACLMessage msg = receive();
				if (msg != null)
				{
					System.out.println(msg.getContent());
					fLogger.info(msg.getContent());

					ACLMessage message = new ACLMessage(7); // INFORM
					message.addReceiver(fDBAgent.getName());
					message.setContent("agent_details:" + fIdentification + "," + fDataModel.getCapacities()[fIdentification]);
					send(message);

					if (msg.getContent().contains("Route: "))
					{
						System.out.println(this.getAgent().getLocalName());
						String array = msg.getContent().split("Route: ")[1];
						array = array.split("\\[")[1];
						array = array.split("\\]")[0];

						fRoute = Arrays.stream(array.split(", ")).mapToInt(Integer::parseInt).toArray();
						System.out.println(Arrays.toString(fRoute) + this.myAgent.getLocalName());

						if (fPosition == null)
						{
							fPosition = new Point(fDataModel.getLocation(fRoute[0]).x, fDataModel.getLocation(fRoute[0]).y);
							System.out.println(fPosition.x + "|" + fPosition.y + "|" + this.myAgent.getLocalName());
							fLocationIndex = 0;
						}

						ACLMessage forward = new ACLMessage(ACLMessage.INFORM);
						message.addReceiver(fDBAgent.getName());
						message.setContent("agent_routes: " + msg.getContent().replace("Route: ", "").trim());
						System.out.println("send routes");
						send(message);
					}

					//continue current path
					if (msg.getContent().contains("continue_path"))
					{

					}
				}
				block();
			}
		});
		
		// advance position
		addBehaviour(new TickerBehaviour(this, 150) 
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onTick()
			{
				if (fDataModel != null && fRoute != null)
				{
					if (fPosition != null && fRoute.length > 0 && (fLocationIndex + 1) < fRoute.length)
					{
						double y_dif = (fDataModel.getLocation(fRoute[fLocationIndex + 1]).y - fPosition.y);
						double x_dif = (fDataModel.getLocation(fRoute[fLocationIndex + 1]).x - fPosition.x);
						double gradient = 0;
						if (!(y_dif == 0 || x_dif == 0))
						{
							gradient = Double.valueOf(y_dif / x_dif);
						}
						else
						{
							if (x_dif == 0)
							{
								gradient = Double.valueOf(y_dif/1);
							}

							if (y_dif == 0 && x_dif == 0)
							{
								fLocationIndex++;

								if ((fLocationIndex + 1) >= fRoute.length)
								{
									y_dif = (fDataModel.getLocation(fRoute[0]).y - fPosition.y);
									x_dif = (fDataModel.getLocation(fRoute[0]).x - fPosition.x);
									return;
								}
								else
								{
									y_dif = (fDataModel.getLocation(fRoute[fLocationIndex + 1]).y - fPosition.y);
									x_dif = (fDataModel.getLocation(fRoute[fLocationIndex + 1]).x - fPosition.x);
									gradient = Double.valueOf(y_dif / x_dif);
								}
							}
						}

						double c = fDataModel.getLocation(fRoute[fLocationIndex + 1]).y - (gradient * fDataModel.getLocation(fRoute[fLocationIndex + 1]).x);

						double x = 0;
						double y = 0;

						double x_offset = 0;
						double y_offset = 0;

						x = getPosX(gradient, c, fPosition.x, fPosition.y, fDataModel.getVehicle(fIdentification).getVelocity());


						if (fPosition.x > x)
						{
							x_offset = fPosition.x - x;
						}
						else
						{
							x_offset = x - fPosition.x;
						}

						if (x_dif < 0)
						{
							x = fPosition.x - x_offset;
							if (x < fDataModel.getLocation(fRoute[fLocationIndex + 1]).x)
							{
								x = fDataModel.getLocation(fRoute[fLocationIndex + 1]).x;
							}
						}
						else
						{
							x = fPosition.x + x_offset;
							if (x > fDataModel.getLocation(fRoute[fLocationIndex + 1]).x)
							{
								x = fDataModel.getLocation(fRoute[fLocationIndex + 1]).x;
							}
						}

						y = getPosY(gradient, x, c);

						if (fPosition.y > y)
						{
							y_offset = fPosition.y - y;
						}
						else
						{
							y_offset = y - fPosition.y;
						}

						if (y_dif > 0)
						{
							y = fPosition.y + y_offset;
							if (y > fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
							{
								y = fDataModel.getLocation(fRoute[fLocationIndex + 1]).y;
							}
						}
						else
						{
							y = fPosition.y - y_offset;
							if (y < fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
							{
								y = fDataModel.getLocation(fRoute[fLocationIndex + 1]).y;
							}
						}

						fPosition.x = (int) x;
						fPosition.y = (int) y;

						if (x_dif > 0)
						{
							if (fPosition.x > fDataModel.getLocation(fRoute[fLocationIndex + 1]).x)
							{
								if (y_dif > 0)
								{
									if (fPosition.y > fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
									{
										fLocationIndex++;
									}
								}
								else
								{
									if (fPosition.y < fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
									{
										fLocationIndex++;
									}
								}
							}
						}
						else
						{
							if (fPosition.x < fDataModel.getLocation(fRoute[fLocationIndex + 1]).x)
							{
								if (y_dif > 0)
								{
									if (fPosition.y > fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
									{
										fLocationIndex++;
									}
								}
								else
								{
									if (fPosition.y < fDataModel.getLocation(fRoute[fLocationIndex + 1]).y)
									{
										fLocationIndex++;
									}
								}
							}
						}

						if ((fLocationIndex) >= fRoute.length)
						{
							fDataModel = null;
							fRoute = null;
							return;
						}
					}

					block();
				}

				block();
			}
		});
		
		// send out agent location
		addBehaviour(new TickerBehaviour(this, 500) 
		{
			private static final long serialVersionUID = 1L;

			protected void onTick()
			{
				if (fDataModel != null && fPosition != null)
				{
					ACLMessage message = new ACLMessage(7); // INFORM
					message.addReceiver(fDBAgent.getName());
					message.setContent("agent_position:" + String.valueOf(fPosition.x) + "," + String.valueOf(fPosition.y));
					//send(message);
				}

				block();
			}

		});
	}
	
	public void findAgents()
	{
		try
		{
			SearchConstraints sc = new SearchConstraints();
			sc.setMaxResults(Long.valueOf(-1)); // not sure of the default value, but this ensures you get them all.
			fAgents = AMSService.search(this, new AMSAgentDescription(), sc);
			for (int i = 0; i < fAgents.length; i++)
			{
				if (fAgents[i].getName().toString().contains("MasterRouteAgent"))
				{
					fMasterAgent = fAgents[i];
					System.out.println(fMasterAgent.getName());
				}

				if (fAgents[i].getName().toString().contains("DBAgent"))
				{
					fDBAgent = fAgents[i];
					System.out.println(fDBAgent.getName());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public double getPosX(double gradient, double c, double start_x, double start_y, double distance)
	{
		return (gradient * start_y - gradient * c + start_x +
				Math.sqrt(-1 * Math.pow(start_y, 2) + 2 * c * start_y + 2 * gradient * start_y * start_x + Math.pow(distance, 2) + Math.pow(distance, 2) * Math.pow(gradient, 2) - Math.pow(gradient, 2) *
						Math.pow(start_x, 2) - 2 * gradient * c * start_x - Math.pow(c, 2)))
				/ (Math.pow(gradient, 2) + 1);
	}

	public double getPosY(double gradient, double x, double c)
	{
		return gradient * x + c;
	}
}
