export interface LoginDto{
    token?: string,
    userId?: number,
    emailId: string,
    role?: string,
    status?: boolean,  
    password:string 
}