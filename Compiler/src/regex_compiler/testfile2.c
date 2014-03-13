#include <stdio.h>
#include <stdlib.h>

// this is a comment

struct node{
           int data;
           struct node *next;
           };
            struct node **createLL();
             struct node **merge(struct node **head1,struct node **head2);
           main()
           {			 int i;

           /*
             This is a multi-line character
             Wow, this is the second line
            */
                         struct node **head1,**head2;
                         head1=createLL();
                         head2=createLL();
                         struct node *temp1=*head1;
                         for(i=1;i<=5;i++)
                         {struct node *new=(struct node *)malloc(sizeof(struct node));
                         if(new==NULL)
                         {
                                                  printf("Error! new");
                                                  exit(0);}
                         scanf("%d",&new->data);
                         temp1->next =new;
                         new->next = NULL;
                         temp1=temp1->next;
                         }
                          struct node *temp2=*head2;
                         for(i=1;i<=5;i++)
                         {struct node *new=(struct node *)malloc(sizeof(struct node));
                          if(new==NULL)
                         {
                                                  printf("Error! in loop");
                                                  exit(0);}
                         scanf("%d",&(new->data));
                         temp2->next = new;
                         new->next=NULL;
                         temp2=temp2->next;
                         }
                         struct node **head3;
                         head3=merge(head1,head2);
                         struct node *temp3=*head3;
                         for(i=0;i<10;i++)
                         {printf("%d->",temp3->data);
                         temp3=temp3->next;
                         }

                         }
                         struct node **createLL(void)
                         {
                                        struct node **head=(struct node **)malloc(sizeof(struct node *));
                                         if(head==NULL)
                         {
                                                  printf("Error! head");
                                                  exit(1);}
                                        *head=NULL;
                                        return head;}
                         struct node **merge(struct node **head1,struct node **head2)
                         {
                                        struct node **head3=createLL();

                                        struct node *temp1=*head1,*temp2=*head2,*temp3=*head3,*temp4=*head3,*pt;

                                          while(temp1!=NULL)
                                          {struct node *new=(struct node *)malloc(sizeof(struct node));
                                          new->data=temp1->data;
                                          new->next=NULL;
                                          temp3->next=new;
                                          temp3=temp3->next;
                                          temp1=temp1->next;
                                          }
                                          while(temp2!=NULL)
                                          {struct node *new=(struct node *)malloc(sizeof(struct node));
                                          new->data=temp2->data;
                                          new->next=NULL;
                                          temp3->next=new;
                                          temp3=temp3->next;
                                          temp2=temp2->next;
                                          }


                                          temp3=*head3;
                                          while(temp3->next!=NULL)
                                          {temp4 = temp3->next;
                                          int min=temp3->data;
                                          pt=temp4;
                                                                           while(temp4!=NULL)
                                                                           {if(min>(temp4->data))
                                          {min=temp4->data;
                                                                                  pt=temp4;}
                                                                                  temp4=temp4->next;
                                                                                  }

                                                                                  int x=temp3->data;
                                                                                  temp3->data=pt->data;
                                                                                  pt->data=x;

                                                                                 temp3=temp3->next; }

                                                                                 return head3;
                                                                                 }

